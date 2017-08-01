package com.maths22.ftcmanuals;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;

public class TestThings {
    public static void main(String[] args) throws IOException {

        Connection connection = Jsoup.connect("https://ftcforum.usfirst.org/activity/get");
        connection.method(Connection.Method.POST);
        connection.ignoreContentType(true);
        connection.data("filters[nodeid]", "5912");
        connection.data("filters[view]", "thread");
        connection.data("filters[nolimit]", "1");
        connection.data("filters[per-page]", "1000");
        connection.data("filters[pagenum]", "1");
        connection.data("filters[userid]", "0");
        connection.data("filters[showChannelInfo]", "1");
        connection.data("filters[filter_time]", "time_all");
        connection.data("filters[filter_show]", "show_all");
        connection.data("securitytoken", "guest");
        Connection.Response resp = connection.execute();
        JSONObject obj = new JSONObject(resp.body());
        Document doc = Jsoup.parseBodyFragment(obj.getString("template"));

        String section = doc.body().getElementsByClass("b-post__content").first()
                .getElementsByClass("b-post__title").text().trim().split(" ?- ?")[0];
        doc.body().getElementsByClass("b-post__content").stream().skip(1).map((e) -> Post.parse(section, e)).forEach(System.out::println);

    }

    public static class Post {
        public static Post parse(String category, Element element) {
            Post ret = new Post();

            ret.title = element.getElementsByClass("b-post__count").first().text() + ": " +
                    element.getElementsByClass("js-post__content-text").first().childNode(0).outerHtml().trim();
            ret.posted = LocalDateTime.parse(element.getElementsByClass("b-post__timestamp").first().getElementsByTag("time").first().attr("datetime"));

            try {
                ret.question = element.getElementsByClass("js-post__content-text").first().getElementsByClass("bbcode_quote").first().getElementsByClass("message").first().html();
            } catch (NullPointerException ignored) {

            }

            Element cleanUp = element.getElementsByClass("js-post__content-text").first().clone();
            Element cleanUpQuote = cleanUp.getElementsByClass("bbcode_quote").first();
            if (cleanUpQuote != null) {
                Elements cleanUpQuoteParents = cleanUpQuote.parents();
                cleanUpQuoteParents.get(cleanUpQuoteParents.size() - 2).remove();
            }
            cleanUp.childNode(0).remove();
            while (cleanUp.childNode(0).outerHtml().trim().equals("") || cleanUp.childNode(0).nodeName().equals("br")) {
                cleanUp.childNode(0).remove();
            }

            ret.answer = cleanUp.html();
            ret.category = category;
            ret.raw = element.html();

            //Rule titles tend to be broken in this way, so unescape them twice
            ret.title = StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeXml(ret.title));

            return ret;
        }

        private String category;
        private String title;
        private String question;
        private String answer;
        private String raw;
        private LocalDateTime posted;

        public String getTitle() {
            return title;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }

        public LocalDateTime getPosted() {
            return posted;
        }

        public String getCategory() {
            return category;
        }

        public String getRaw() {
            return raw;
        }


        @Override
        public String toString() {
            return "Post{" +
                    "category='" + category + '\'' +
                    ", title='" + title + '\'' +
                    ", posted=" + posted +
                    '}';
        }
    }
}

/*
public static TestThings.Post parse(String category, Element element) {
        TestThings.Post ret = new TestThings.Post();

        ret.title = element.getElementsByClass("b-post__count").first().text() + ": " +
                element.getElementsByClass("js-post__content-text").first().childNode(0).outerHtml().trim();
        ret.posted = LocalDateTime.parse(element.getElementsByClass("b-post__timestamp").first().getElementsByTag("time").first().attr("datetime"));

        try {
            ret.question = element.getElementsByClass("js-post__content-text").first().getElementsByClass("bbcode_quote").first().getElementsByClass("message").first().html();
        } catch (NullPointerException ignored) {

        }

        Element cleanUp = element.getElementsByClass("js-post__content-text").first().clone();
        Element cleanUpQuote = cleanUp.getElementsByClass("bbcode_quote").first();
        if(cleanUpQuote != null) {
            Elements cleanUpQuoteParents = cleanUpQuote.parents();
            cleanUpQuoteParents.get(cleanUpQuoteParents.size() - 2).remove();
        }
        cleanUp.childNode(0).remove();
        while(cleanUp.childNode(0).outerHtml().trim().equals("") || cleanUp.childNode(0).nodeName().equals("br")) {
            cleanUp.childNode(0).remove();
        }

        ret.answer = cleanUp.html();
        ret.category = category;
        ret.raw = element.html();

        //Rule titles tend to be broken in this way, so unescape them twice
        ret.title = StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeXml(ret.title));

        return ret;
    }
 */