package me.tweirtx.ruleparser;

import com.maths22.ftcmanuals.models.GameManualSource;
import com.maths22.ftcmanuals.models.Rule;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GameManualImporter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Connection database;

    private static final String manualURL = "https://www.firstinspires.org/sites/default/files/uploads/resource_library/ftc/game-manual-part-1.pdf";

    private final String ruleset = "ftc2020";

    public GameManualImporter(Connection database) {
        this.database = database;
    }

    public static void main(String[] args) throws SQLException {
        //String url = "jdbc:postgresql://localhost/rules?user=rules&password=rulesapi";
        String url = "jdbc:sqlite:test.db";
        Connection conn = DriverManager.getConnection(url);
        GameManualImporter gameManualImporter = new GameManualImporter(conn);
        GameManualSource gameManualSource = new GameManualSource();
        gameManualSource.setUrl(manualURL);
        boolean success = gameManualImporter.importGameManual(gameManualSource);
        System.out.println("Parse success: " + success);
    }

    public boolean importGameManual(GameManualSource gm) {


        String content;
        try {
            InputStream instr = new URL(gm.getUrl()).openStream();
            PDDocument pddDocument = PDDocument.load(instr);
            PDFTextStripper textStripper = new PDFTextStripper();
            content = textStripper.getText(pddDocument);
            pddDocument.close();
        } catch (IOException e) {
            log.warn("Could not download game manual", e);
            return false;
        }

        Matcher versionMatcher = Pattern.compile("Revision ([0-9.]+)").matcher(content);
        // TODO: handle failure
        boolean b = versionMatcher.find();
        if (b) {
            System.out.println("Found version");
        }
        String version = versionMatcher.group(1);

        //Remove right-hand header/footer
        content = content.replaceAll("[0-9]+ \\| .*(\\s)+Revision [0-9]+.*[\\r\\n]*", "");
        //Remove left-hand header/footer
        content = content.replaceAll(".* \\| [0-9]+(\\s)+Gracious Professionalism® -.*[\\r\\n]*", "");

        List<String> sectionHeadings = new ArrayList<>();
        Matcher headingMatcher = Pattern.compile("^\\S+ .*?(?= ?\\.\\.+ [0-9]+)", Pattern.MULTILINE)
                .matcher(content);
        while (headingMatcher.find()) {
            // Don't include the contents themselves!
            if(headingMatcher.group().trim().toLowerCase().equals("contents")) continue;
            sectionHeadings.add(headingMatcher.group().trim());
        }
        //      In case I want hierarchies at some point
        //        sectionHeadings.stream().filter((s) -> s.matches("([0-9.]+) (.*)")).forEach((s) -> {
        //            Matcher m = Pattern.compile("([0-9.]+) (.*)").matcher(s);
        //            m.find();
        //            String[] pieces = m.group(1).split("\\.");
        //            if(pieces.length == 2 && pieces[1].equals("0")) {
        //                pieces = new String[]{pieces[0]};
        //            }
        //            String name = m.group(2);
        //            System.out.println(Arrays.toString(pieces) + ": " + name);
        //        });
        if(sectionHeadings.get(0).toLowerCase().equals("contents")) {
            sectionHeadings.remove(0);
        }
        //Body starts w/ second instance of heading (outside of TOC)
        Matcher bodyMatcher = Pattern.compile(Pattern.quote(sectionHeadings.get(0)) + ".*?" + Pattern.quote(sectionHeadings.get(sectionHeadings.size() - 1)) + ".*?" + "(" + Pattern.quote(sectionHeadings.get(0)) + ".*)", Pattern.DOTALL).matcher(content);
        // TODO: handle failure
        boolean success = bodyMatcher.find();
        if (success) {
            System.out.println("Body match success");
        }
        String bodyContent = bodyMatcher.group(1);

        Map<String, String> sections = getStringsBetween(sectionHeadings, bodyContent);


        List<String> ruleHeadings = sectionHeadings.stream().filter((s) -> s.contains("Rules")).collect(Collectors.toList());
        ruleHeadings.forEach((s) -> {
            String cleanHeading = s.replace("[0-9.]+\\s*", "");
            List<Rule> rules = parseRules(sections.get(s)).stream().peek((rule) -> {
                rule.setCategory(cleanHeading);
                rule.setId(cleanHeading.hashCode() + ":" + rule.getNumber().hashCode());
                rule.setVersion(version);
            }).collect(Collectors.toList());
            if (rules.size() > 0) {
                try {
                    String ruleStr = rules.toString().replaceAll("\\{gametag}", ruleset);
                    System.out.println(ruleStr);
                    database.prepareStatement("INSERT INTO rules VALUES (" + ruleStr + ");");
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });

        return true;
    }


    private Map<String, String> getStringsBetween(List<String> headings, String content) {
        Map<String, String> ret = new HashMap<>();
        for (int i = 0; i < headings.size() - 1; i++) {
            String hdg1 = headings.get(i);
            String hdg2 = headings.get(i + 1);
            Matcher sectionMatcher = Pattern.compile("^[ \t]*(" + Pattern.quote(hdg1) + ".*)^[ \t]*" + Pattern.quote(hdg2), Pattern.DOTALL | Pattern.MULTILINE).matcher(content);
            // TODO: handle failure
            if (sectionMatcher.find()) {
                System.out.println("Section matched");
            }
            try {
                String section = sectionMatcher.group(1);
                ret.put(hdg1, section);
            }
            catch (IllegalStateException e) {
                System.out.println("Oops");
            }
        }
        String lastHdg = headings.get(headings.size() - 1);
        Matcher lastSectionMatcher = Pattern.compile("^[ \t]*(" + Pattern.quote(lastHdg) + ".*)", Pattern.DOTALL | Pattern.MULTILINE).matcher(content);
        // TODO: handle failure
        if (lastSectionMatcher.find()) {
            System.out.println("Last section matched");
        }
        String lastSection = lastSectionMatcher.group(1);
        ret.put(lastHdg, lastSection);
        return ret;
    }

    private List<Rule> parseRules(String section) {
        List<Rule> ret = new ArrayList<>();
        boolean done;
        String remainder = section;
        while (true) {
            Matcher defMatcher = Pattern.compile("^(<[A-Z]+[0-9]+>) ?(.*?)^(^<[A-Z]+[0-9]+> ?.*)", Pattern.DOTALL | Pattern.MULTILINE).matcher(remainder);
            done = !defMatcher.find();
            if (done) break;
            String number = defMatcher.group(1);
            String[] bodyParts = defMatcher.group(2).split(" [–-] ");
            Rule rule = new Rule();
            rule.setNumber(number);
            if (bodyParts.length == 1) {
                rule.setBody(bodyParts[0].replaceAll("[\\r\\n]+(?!([a-z0-9]{1,3}\\.|[\\uF0B7\\u2022]))", " "));
            } else {
                rule.setTitle(bodyParts[0]);
                rule.setBody(bodyParts[1].replaceAll("[\\r\\n]+(?!([a-z0-9]{1,3}\\.|[\\uF0B7\\u2022]))", " "));
            }
            ret.add(rule);
            remainder = defMatcher.group(3);
        }
        Matcher lastMatcher = Pattern.compile("^(<[A-Z]+[0-9]+>) ?(.*)", Pattern.DOTALL | Pattern.MULTILINE).matcher(remainder);
        done = !lastMatcher.find();
        if (done) return ret;
        String title = lastMatcher.group(1);
        String[] bodyParts = lastMatcher.group(2).split(" [–-] ");
        Rule rule = new Rule();
        rule.setNumber(title);
        if (bodyParts.length == 1) {
            rule.setBody(bodyParts[0].replaceAll("[\\r\\n]+(?!([a-z0-9]{1,3}\\.|[\\uF0B7\\u2022]))", " "));
        } else {
            rule.setTitle(bodyParts[0]);
            rule.setBody(bodyParts[1].replaceAll("[\\r\\n]+(?!([a-z0-9]{1,3}\\.|[\\uF0B7\\u2022]))", " "));
        }
        ret.add(rule);
        return ret;
    }
}
