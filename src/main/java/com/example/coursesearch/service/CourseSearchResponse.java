package com.example.coursesearch.service;

import com.example.coursesearch.document.CourseDocument;

import java.util.List;

public class CourseSearchResponse {
    private long total;
    private List<CourseDocument> courses;


    public CourseSearchResponse(long total, List<CourseDocument> courses) {
        this.total = total;
        this.courses = courses;
    }

    public CourseSearchResponse() {}


    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<CourseDocument> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseDocument> courses) {
        this.courses = courses;
    }
}
