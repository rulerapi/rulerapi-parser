package com.maths22.ftcmanuals.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.completion.Completion;

@Document(indexName = "ftc-manuals-texts", type = "rule")
@Setting(settingPath = "/elasticsearch/number-analyzer.json")
public class Rule {
    @Id
    private String id;
    @MultiField(
            mainField = @Field(type = FieldType.text, analyzer = "english"),
            otherFields = {@InnerField(suffix = "keyword", type = FieldType.keyword)}
    )
    private String category;
    @Field(type = FieldType.keyword)
    private String number;
    @MultiField(
            mainField = @Field(type = FieldType.text, analyzer = "english"),
            otherFields = {@InnerField(suffix = "keyword", type = FieldType.keyword)}
    )
    private String title;
    @Field(type = FieldType.text, analyzer = "english")
    private String body;
    @Field(type = FieldType.keyword)
    private String version;
    @CompletionField(analyzer = "standard", searchAnalyzer = "standard")
    private Completion suggest;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
        this.suggest = new Completion(new String[]{number});
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Completion getSuggest() {
        return suggest;
    }

    protected void setSuggest(Completion suggest) {
        this.suggest = suggest;
    }
}
