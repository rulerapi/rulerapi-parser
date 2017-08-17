package com.maths22.ftcmanuals.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Document(indexName = "ftc-manuals-texts", type = "forum-post")
@Setting(settingPath = "/elasticsearch/number-analyzer.json")
public class ForumPost {
    @Id
    private String id;
    @Field(type = FieldType.keyword)
    private String forum;
    @MultiField(
            mainField = @Field(type = FieldType.text, analyzer = "english"),
            otherFields = {@InnerField(suffix = "keyword", type = FieldType.keyword)}
    )
    private String category;
    @Field(type = FieldType.Integer)
    private int postNo;
    @MultiField(
            mainField = @Field(type = FieldType.text, analyzer = "english"),
            otherFields = {@InnerField(suffix = "keyword", type = FieldType.keyword)}
    )
    private String title;
    @Field(type = FieldType.text, analyzer = "english")
    private String question;
    @Field(type = FieldType.text, analyzer = "english")
    private String answer;
    private String raw;
    @Field(type = FieldType.Date)
    private LocalDateTime posted;
    @Field(type = FieldType.keyword)
    private String version;
    // TODO: should these be searchable?
    private String author;
    private String questionAuthor;

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public LocalDateTime getPosted() {
        return posted;
    }

    public void setPosted(LocalDateTime posted) {
        this.posted = posted;
    }

    public int getPostNo() {
        return postNo;
    }

    public void setPostNo(int postNo) {
        this.postNo = postNo;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getQuestionAuthor() {
        return questionAuthor;
    }

    public void setQuestionAuthor(String questionAuthor) {
        this.questionAuthor = questionAuthor;
    }
}
