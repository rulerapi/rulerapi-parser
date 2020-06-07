package com.maths22.ftcmanuals.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.completion.Completion;

@Document(indexName = "ftc-manuals-texts", type = "text", shards = 1, replicas = 0)
public class Rule {
    @JsonProperty
    private final String type = Rule.class.getSimpleName();

    @Id
    private String id;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "english"),
            otherFields = {@InnerField(suffix = "keyword", type = FieldType.Keyword)}
    )
    private String category;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = {@InnerField(suffix = "keyword", type = FieldType.Keyword)}
    )
    private String number;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "english"),
            otherFields = {@InnerField(suffix = "keyword", type = FieldType.Keyword)}
    )
    private String title;
    @Field(type = FieldType.Text, analyzer = "english")
    private String body;
    @Field(type = FieldType.Keyword)
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
        this.number = number.replace('<', ' ').replace('>', ' ').trim();
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

    @Override
    public String toString() {
        return "('" + getNumber() + "', '" + getBody() + "', '{gametag}')";
    }

    protected void setSuggest(Completion suggest) {
        this.suggest = suggest;
    }
}
