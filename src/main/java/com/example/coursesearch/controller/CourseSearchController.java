package com.example.coursesearch.controller;

import com.example.coursesearch.service.CourseSearchRequest;
import com.example.coursesearch.service.CourseSearchResponse;
import com.example.coursesearch.service.CourseSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class CourseSearchController {

    private static final Logger log = LoggerFactory.getLogger(CourseSearchController.class);

    private final CourseSearchService courseSearchService;

    public CourseSearchController(CourseSearchService courseSearchService) {
        this.courseSearchService = courseSearchService;
    }

    /**
     * Assignment A – Main search endpoint.
     *
     * GET /api/search?q=algebra&minAge=10&maxAge=15&category=Math&type=COURSE
     *                &minPrice=20&maxPrice=100&startDate=2025-06-01T00:00:00Z
     *                &sort=priceAsc&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<CourseSearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false, defaultValue = "upcoming") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        log.info("Search request: q={}, minAge={}, maxAge={}, category={}, type={}, " +
                        "minPrice={}, maxPrice={}, startDate={}, sort={}, page={}, size={}",
                q, minAge, maxAge, category, type, minPrice, maxPrice, startDate, sort, page, size);

        CourseSearchRequest request = new CourseSearchRequest();
        request.setQ(q);
        request.setMinAge(minAge);
        request.setMaxAge(maxAge);
        request.setCategory(category);
        request.setType(type);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setStartDate(startDate);
        request.setSort(sort);
        request.setPage(page);
        request.setSize(size);

        CourseSearchResponse response = courseSearchService.search(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Assignment B – Autocomplete suggestions endpoint.
     *
     * GET /api/search/suggest?q=phy
     */
    @GetMapping("/suggest")
    public ResponseEntity<List<String>> suggest(@RequestParam String q) {
        log.info("Suggest request: q={}", q);
        List<String> suggestions = courseSearchService.suggest(q);
        return ResponseEntity.ok(suggestions);
    }
}