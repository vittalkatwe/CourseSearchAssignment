package com.example.coursesearch.service;

import com.example.coursesearch.document.CourseDocument;
import com.example.coursesearch.repository.CourseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class DataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionService.class);

    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    public DataIngestionService(CourseRepository courseRepository, ObjectMapper objectMapper) {
        this.courseRepository = courseRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void ingestData() {
        try {
            // Delete existing data and re-index every startup (ensures fresh data)
            courseRepository.deleteAll();
            log.info("Cleared existing courses from index.");

            log.info("Starting bulk ingestion of sample course data...");

            ClassPathResource resource = new ClassPathResource("sample-courses.json");

            if (!resource.exists()) {
                log.error("sample-courses.json NOT FOUND in classpath! Check it is in src/main/resources/");
                return;
            }

            InputStream is = resource.getInputStream();
            List<CourseDocument> courses = objectMapper.readValue(is, new TypeReference<List<CourseDocument>>() {});

            if (courses == null || courses.isEmpty()) {
                log.error("No courses parsed from sample-courses.json — check the JSON format!");
                return;
            }

            log.info("Parsed {} courses from JSON. Indexing...", courses.size());

            // Populate suggest field for autocomplete
            for (CourseDocument course : courses) {
                if (course.getTitle() != null) {
                    course.setSuggest(new Completion(new String[]{course.getTitle()}));
                }
            }

            courseRepository.saveAll(courses);

            long count = courseRepository.count();
            log.info("Ingestion complete. Total courses now in Elasticsearch: {}", count);

        } catch (Exception e) {
            log.error("Failed to ingest sample data: {}", e.getMessage(), e);
        }
    }
}