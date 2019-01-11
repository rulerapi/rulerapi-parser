package com.maths22.ftcmanuals.services;

import com.maths22.ftcmanuals.models.ForumPost;
import com.maths22.ftcmanuals.models.VBulletinForumSource;
import com.maths22.ftcmanuals.repositories.elasticsearch.ForumPostEsRepository;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VBulletinForumImporter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ForumPostEsRepository forumPostEsRepository;

    @Autowired
    public VBulletinForumImporter(ForumPostEsRepository forumPostEsRepository) {
        this.forumPostEsRepository = forumPostEsRepository;
    }

    public boolean importForumThread(VBulletinForumSource vb) {
        Connection connection = Jsoup.connect(vb.getDomain() + "/activity/get");
        connection.method(Connection.Method.POST);
        connection.ignoreContentType(true);
        connection.data("filters[nodeid]", vb.getNodeId());
        connection.data("filters[view]", "thread");
        connection.data("filters[nolimit]", "1");
        connection.data("filters[per-page]", "1000");
        connection.data("filters[pagenum]", "1");
        connection.data("filters[userid]", "0");
        connection.data("filters[showChannelInfo]", "1");
        connection.data("filters[filter_time]", "time_all");
        connection.data("filters[filter_show]", "show_all");
        connection.data("securitytoken", "guest");
        Connection.Response resp;
        try {
            resp = connection.execute();
        } catch (IOException e) {
            log.warn("Could not download forum thread", e);
            return false;
        }

        JSONObject obj = new JSONObject(resp.body());
        Document doc = Jsoup.parseBodyFragment(obj.getString("template"));

        String section = doc.body().getElementsByClass("b-post__content").first()
                .getElementsByClass("b-post__title").text().trim().split(" ?- ?Answer")[0];
        List<ForumPost> forumPosts = doc.body().getElementsByClass("b-post").stream().skip(1).map((e) -> parse(section, e))
                .peek((e) -> {
                    e.setForum("ftcForum");
                    // TODO better versioning
                    e.setVersion("1");
                }).collect(Collectors.toList());

        if(forumPosts.size() > 0) {
            forumPostEsRepository.saveAll(forumPosts);
        }

        return true;
    }

    private ForumPost parse(String category, Element element) {
        ForumPost ret = new ForumPost();

        boolean isSubpost = element.getElementsByClass("author").size() == 0;

        ret.setAuthor(element.getElementsByClass(isSubpost ? "OLD__author" : "author").first().text());
        Element postElement = element.getElementsByClass(isSubpost ? "OLD__post-content" : "b-post__content").first();
        if(isSubpost) postElement = postElement.parent();

        String postNo = element.getElementsByClass("b-post__count").first().text();
        ret.setId("ftcForum:" + category.hashCode() + ":" + postNo);
        ret.setPostNo(postNo.replaceAll("[^0-9.]",""));
//        ret.setPosted(LocalDateTime.parse(postElement
//                .getElementsByClass("b-post__timestamp").first()
//                .getElementsByTag("time").first()
//                .attr("datetime")));
        String dateString = element
                .getElementsByClass("b-post__timestamp").first()
                .text();

        if(dateString.contains("Yesterday")) {
            String timeString = dateString.replace("Yesterday, ", "");
            ret.setPosted(LocalDateTime.of(LocalDate.now().minusDays(1),
                    LocalTime.parse(timeString, DateTimeFormatter.ofPattern("hh:mm a"))));
        } else if(dateString.contains("Today")) {
            String timeString = dateString.replace("Today, ", "");
            ret.setPosted(LocalDateTime.of(LocalDate.now(),
                    LocalTime.parse(timeString, DateTimeFormatter.ofPattern("hh:mm a"))));
        } else {
            ret.setPosted(LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("MM-dd-yyy, hh:mm a")));
        }

        boolean foundQuestion = false;
        try {
            String title = postElement
                    .getElementsByClass(isSubpost ? "OLD__post-content" : "js-post__content-text").first()
                    .getElementsByClass("bbcode_quote").first()
                    .getElementsByClass("message").first()
                    .getElementsByTag("b").first()
                    .html();
            if(title.trim().length() > 0) {
                ret.setTitle(title.trim());
            }
            //Remove title
            postElement
                    .getElementsByClass(isSubpost ? "OLD__post-content" : "js-post__content-text").first()
                    .getElementsByClass("bbcode_quote").first()
                    .getElementsByClass("message").first()
                    .getElementsByTag("b").first().remove();
            ret.setQuestion(postElement
                    .getElementsByClass(isSubpost ? "OLD__post-content" : "js-post__content-text").first()
                    .getElementsByClass("bbcode_quote").first()
                    .getElementsByClass("message").first()
                    .html());
            ret.setQuestionAuthor(postElement
                    .getElementsByClass(isSubpost ? "OLD__post-content" : "js-post__content-text").first()
                    .getElementsByClass("bbcode_quote").first()
                    .getElementsByClass("bbcode_postedby").first()
                    .getElementsByTag("strong").first()
                    .text());
            foundQuestion = true;
        } catch (NullPointerException ignored) {

        }

        if(!foundQuestion) {
            try {
                ret.setQuestion(postElement
                        .getElementsByClass(isSubpost ? "bbcode_container" : "js-post__content-text").first()
                        .getElementsByClass("bbcode_quote").first()
                        .getElementsByClass("quote_container").first()
                        .html());
            } catch (NullPointerException ignored) {

            }
        }

        Element cleanUp = postElement.getElementsByClass(isSubpost ? "OLD__post-content" : "js-post__content-text").first().clone();
        Element cleanUpQuote = cleanUp.getElementsByClass("bbcode_quote").first();
        if (cleanUpQuote != null) {
            Elements cleanUpQuoteParents = cleanUpQuote.parents();
            cleanUpQuoteParents.get(cleanUpQuoteParents.size() - 2).remove();
        }
        cleanUp.childNode(0).remove();
        while (cleanUp.childNode(0).outerHtml().trim().equals("") || cleanUp.childNode(0).nodeName().equals("br")) {
            cleanUp.childNode(0).remove();
        }

        ret.setAnswer(cleanUp.html());
        ret.setCategory(category);
        ret.setRaw(element.html());

        //Rule titles tend to be broken in this way, so unescape them twice
        ret.setTitle(StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeXml(ret.getTitle())));

        return ret;
    }
}
