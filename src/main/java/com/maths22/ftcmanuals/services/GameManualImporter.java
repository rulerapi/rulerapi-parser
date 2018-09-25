package com.maths22.ftcmanuals.services;

import com.maths22.ftcmanuals.models.Definition;
import com.maths22.ftcmanuals.models.GameManualSource;
import com.maths22.ftcmanuals.models.Rule;
import com.maths22.ftcmanuals.repositories.elasticsearch.DefinitionEsRepository;
import com.maths22.ftcmanuals.repositories.elasticsearch.RuleEsRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class GameManualImporter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DefinitionEsRepository definitionEsRepository;

    private final RuleEsRepository ruleEsRepository;

    @Autowired
    public GameManualImporter(DefinitionEsRepository definitionEsRepository, RuleEsRepository ruleEsRepository) {
        this.definitionEsRepository = definitionEsRepository;
        this.ruleEsRepository = ruleEsRepository;
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
        versionMatcher.find();
        String version = versionMatcher.group(1);

        //Remove right-hand header/footer
        content = content.replaceAll("[0-9]+ \\| .*(\\s)+Revision [0-9]+.*[\\r\\n]*", "");
        //Remove left-hand header/footer
        content = content.replaceAll(".* \\| [0-9]+(\\s)+Gracious Professionalism® -.*[\\r\\n]*", "");

        List<String> sectionHeadings = new ArrayList<>();
        Matcher headingMatcher = Pattern.compile("^\\S+ .*?(?= ?\\.\\.+ [0-9]+)", Pattern.MULTILINE)
                .matcher(content);
        while (headingMatcher.find()) {
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
        bodyMatcher.find();
        String bodyContent = bodyMatcher.group(1);

        Map<String, String> sections = getStringsBetween(sectionHeadings, bodyContent);


        List<String> definitionHeadings = sectionHeadings.stream().filter((s) -> s.contains("Definitions")).collect(Collectors.toList());
        definitionHeadings.forEach((s) -> {
            String cleanHeading = s.replace("[0-9.]+\\s*", "");
            List<Definition> definitions = parseDefinitions(sections.get(s)).stream().peek((def) -> {
                def.setCategory(cleanHeading);
                def.setId(cleanHeading.hashCode() + ":" + def.getTitle().hashCode());
                def.setVersion(version);
            }).collect(Collectors.toList());
            if (definitions.size() > 0) {
                definitionEsRepository.saveAll(definitions);
            }
        });

        List<String> ruleHeadings = sectionHeadings.stream().filter((s) -> s.contains("Rules")).collect(Collectors.toList());
        ruleHeadings.forEach((s) -> {
            String cleanHeading = s.replace("[0-9.]+\\s*", "");
            List<Rule> rules = parseRules(sections.get(s)).stream().peek((rule) -> {
                rule.setCategory(cleanHeading);
                rule.setId(cleanHeading.hashCode() + ":" + rule.getNumber().hashCode());
                rule.setVersion(version);
            }).collect(Collectors.toList());
            if (rules.size() > 0) {
                ruleEsRepository.saveAll(rules);
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
            sectionMatcher.find();
            String section = sectionMatcher.group(1);
            ret.put(hdg1, section);
        }
        String lastHdg = headings.get(headings.size() - 1);
        Matcher lastSectionMatcher = Pattern.compile("^[ \t]*(" + Pattern.quote(lastHdg) + ".*)", Pattern.DOTALL | Pattern.MULTILINE).matcher(content);
        // TODO: handle failure
        lastSectionMatcher.find();
        String lastSection = lastSectionMatcher.group(1);
        ret.put(lastHdg, lastSection);
        return ret;
    }

    private List<Definition> parseDefinitions(String section) {
        List<Definition> ret = new ArrayList<>();
        boolean done;
        String remainder = section;
        //Make the space after the dash optional due to a typo in the manual
        while (true) {
            Matcher defMatcher = Pattern.compile("^([^\\r\\n\\uF0B7\\u2022]*?) [-–] ?(.*?)^([^\\r\\n\\uF0B7\\u2022]*? [-–] ?.*)", Pattern.DOTALL | Pattern.MULTILINE).matcher(remainder);
            done = !defMatcher.find();
            if (done) break;
            String term = defMatcher.group(1);
            String body = defMatcher.group(2).replaceAll("[\\r\\n](?!\\s*\\u2022)", " ");
            Definition def = new Definition();
            def.setTitle(term);
            def.setBody(body);
            ret.add(def);
            remainder = defMatcher.group(3);
        }
        Matcher lastMatcher = Pattern.compile("^([^\\r\\n\\uF0B7\\u2022]*?) [-–] ?(.*)", Pattern.DOTALL | Pattern.MULTILINE).matcher(remainder);
        done = !lastMatcher.find();
        if (done) return ret;
        String term = lastMatcher.group(1);
        String body = lastMatcher.group(2).replaceAll("[\\r\\n]+(?!\\s*)", " ");
        Definition def = new Definition();
        def.setTitle(term);
        def.setBody(body);
        ret.add(def);
        return ret;
    }

    private List<Rule> parseRules(String section) {
        List<Rule> ret = new ArrayList<>();
        boolean done;
        String remainder = section;
        while (true) {
            Matcher defMatcher = Pattern.compile("^(<[A-Z]+[0-9]+>) (.*?)^(^<[A-Z]+[0-9]+> .*)", Pattern.DOTALL | Pattern.MULTILINE).matcher(remainder);
            done = !defMatcher.find();
            if (done) break;
            String number = defMatcher.group(1);
            String[] bodyParts = defMatcher.group(2).split(" [–-] ");
            Rule rule = new Rule();
            rule.setNumber(number);
            if (bodyParts.length == 1) {
                rule.setBody(bodyParts[0]);
            } else {
                rule.setTitle(bodyParts[0]);
                rule.setBody(bodyParts[1]);
            }
            ret.add(rule);
            remainder = defMatcher.group(3);
        }
        Matcher lastMatcher = Pattern.compile("^(<[A-Z]+[0-9]+>) (.*)", Pattern.DOTALL | Pattern.MULTILINE).matcher(remainder);
        done = !lastMatcher.find();
        if (done) return ret;
        String title = lastMatcher.group(1);
        String[] bodyParts = lastMatcher.group(2).split(" [–-] ");
        Rule rule = new Rule();
        rule.setNumber(title);
        if (bodyParts.length == 1) {
            rule.setBody(bodyParts[0]);
        } else {
            rule.setTitle(bodyParts[0]);
            rule.setBody(bodyParts[1]);
        }
        ret.add(rule);
        return ret;
    }
}
