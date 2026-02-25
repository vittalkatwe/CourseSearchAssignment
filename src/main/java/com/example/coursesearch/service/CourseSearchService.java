package com.example.coursesearch.service;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.SuggestFuzziness;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.json.JsonData;
import com.example.coursesearch.document.CourseDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseSearchService {

    private static final Logger log = LoggerFactory.getLogger(CourseSearchService.class);
    private static final String INDEX = "courses";

    private final ElasticsearchTemplate elasticsearchTemplate;

    public CourseSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchTemplate = (ElasticsearchTemplate) elasticsearchOperations;
    }

    public CourseSearchResponse search(CourseSearchRequest request) {
        List<Query> filters = new ArrayList<>();

        if (request.getQ() != null && !request.getQ().isBlank()) {
            filters.add(Query.of(q -> q
                    .multiMatch(mm -> mm
                            .query(request.getQ())
                            .fields("title^3", "description")
                            .fuzziness("AUTO")
                            .prefixLength(1)
                    )
            ));
        }

        if (request.getMinAge() != null) {
            JsonData minAgeVal = JsonData.of(request.getMinAge());
            RangeQuery minAgeRange = new RangeQuery.Builder()
                    .field("maxAge")
                    .gte(minAgeVal)
                    .build();
            filters.add(minAgeRange._toQuery());
        }

        if (request.getMaxAge() != null) {
            JsonData maxAgeVal = JsonData.of(request.getMaxAge());
            RangeQuery maxAgeRange = new RangeQuery.Builder()
                    .field("minAge")
                    .lte(maxAgeVal)
                    .build();
            filters.add(maxAgeRange._toQuery());
        }

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            filters.add(Query.of(q -> q
                    .term(t -> t.field("category").value(request.getCategory()))
            ));
        }

        if (request.getType() != null && !request.getType().isBlank()) {
            filters.add(Query.of(q -> q
                    .term(t -> t.field("type").value(request.getType()))
            ));
        }

        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            RangeQuery.Builder priceBuilder = new RangeQuery.Builder().field("price");
            if (request.getMinPrice() != null) priceBuilder.gte(JsonData.of(request.getMinPrice()));
            if (request.getMaxPrice() != null) priceBuilder.lte(JsonData.of(request.getMaxPrice()));
            filters.add(priceBuilder.build()._toQuery());
        }

        if (request.getStartDate() != null) {
            JsonData dateVal = JsonData.of(request.getStartDate().toString());
            RangeQuery dateRange = new RangeQuery.Builder()
                    .field("nextSessionDate")
                    .gte(dateVal)
                    .build();
            filters.add(dateRange._toQuery());
        }

        Query boolQuery = Query.of(q -> q
                .bool(b -> b.must(filters))
        );

        List<SortOptions> sortOptions = buildSortOptions(request.getSort());
        int from = request.getPage() * request.getSize();

        SearchRequest esRequest = SearchRequest.of(sr -> sr
                .index(INDEX)
                .query(boolQuery)
                .sort(sortOptions)
                .from(from)
                .size(request.getSize())
        );

        try {
            SearchResponse<CourseDocument> response =
                    elasticsearchTemplate.execute(client -> client.search(esRequest, CourseDocument.class));

            long total = response.hits().total() != null ? response.hits().total().value() : 0;
            List<CourseDocument> courses = response.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());

            return new CourseSearchResponse(total, courses);
        } catch (Exception e) {
            log.error("Search failed: {}", e.getMessage(), e);
            return new CourseSearchResponse(0, List.of());
        }
    }

    public List<String> suggest(String partialTitle) {
        try {
            Suggester suggester = Suggester.of(s -> s
                    .suggesters("title-suggest", FieldSuggester.of(fs -> fs
                            .completion(c -> c
                                    .field("suggest")
                                    .size(10)
                                    .skipDuplicates(true)
                                    .fuzzy(SuggestFuzziness.of(f -> f.fuzziness("AUTO")))
                            )
                    ))
                    .text(partialTitle)
            );

            SearchRequest esRequest = SearchRequest.of(sr -> sr
                    .index(INDEX)
                    .suggest(suggester)
                    .size(0)
            );

            SearchResponse<CourseDocument> response =
                    elasticsearchTemplate.execute(client -> client.search(esRequest, CourseDocument.class));

            List<String> suggestions = new ArrayList<>();
            if (response.suggest() != null && response.suggest().containsKey("title-suggest")) {
                response.suggest().get("title-suggest").forEach(suggestion ->
                        suggestion.completion().options().stream()
                                .map(CompletionSuggestOption::text)
                                .forEach(suggestions::add)
                );
            }

            return suggestions;
        } catch (Exception e) {
            log.error("Suggest failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private List<SortOptions> buildSortOptions(String sort) {
        List<SortOptions> sortOptions = new ArrayList<>();
        if ("priceAsc".equalsIgnoreCase(sort)) {
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("price").order(SortOrder.Asc))));
        } else if ("priceDesc".equalsIgnoreCase(sort)) {
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("price").order(SortOrder.Desc))));
        } else {
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("nextSessionDate").order(SortOrder.Asc))));
        }
        return sortOptions;
    }
}