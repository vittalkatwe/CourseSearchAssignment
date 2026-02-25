package com.example.coursesearch.service;

import java.time.Instant;

public class CourseSearchRequest {
    private String q;
    private Integer minAge;
    private Integer maxAge;
    private String category;
    private String type;
    private Double minPrice;
    private Double maxPrice;
    private Instant startDate;
    private String sort;
    private int page;
    private int size;

    public CourseSearchRequest() {}

    public String getQ() { return q; }
    public Integer getMinAge() { return minAge; }
    public Integer getMaxAge() { return maxAge; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public Double getMinPrice() { return minPrice; }
    public Double getMaxPrice() { return maxPrice; }
    public Instant getStartDate() { return startDate; }
    public String getSort() { return sort; }
    public int getPage() { return page; }
    public int getSize() { return size; }

    public void setQ(String q) { this.q = q; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }
    public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }
    public void setCategory(String category) { this.category = category; }
    public void setType(String type) { this.type = type; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }
    public void setSort(String sort) { this.sort = sort; }
    public void setPage(int page) { this.page = page; }
    public void setSize(int size) { this.size = size; }
}