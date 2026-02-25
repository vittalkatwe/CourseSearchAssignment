package com.example.coursesearch.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "courses")
public class CourseDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String gradeRange;

    @Field(type = FieldType.Integer)
    private int minAge;

    @Field(type = FieldType.Integer)
    private int maxAge;

    @Field(type = FieldType.Double)
    private double price;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant nextSessionDate;

    @CompletionField(maxInputLength = 100)
    private Completion suggest;

    public CourseDocument() {}

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getGradeRange() { return gradeRange; }
    public int getMinAge() { return minAge; }
    public int getMaxAge() { return maxAge; }
    public double getPrice() { return price; }
    public Instant getNextSessionDate() { return nextSessionDate; }
    public Completion getSuggest() { return suggest; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setType(String type) { this.type = type; }
    public void setGradeRange(String gradeRange) { this.gradeRange = gradeRange; }
    public void setMinAge(int minAge) { this.minAge = minAge; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }
    public void setPrice(double price) { this.price = price; }
    public void setNextSessionDate(Instant nextSessionDate) { this.nextSessionDate = nextSessionDate; }
    public void setSuggest(Completion suggest) { this.suggest = suggest; }
}