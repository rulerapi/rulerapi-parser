package com.maths22.ftcmanuals.repositories.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maths22.ftcmanuals.models.Definition;
import com.maths22.ftcmanuals.models.ForumPost;
import com.maths22.ftcmanuals.models.Rule;
import com.maths22.ftcmanuals.resources.Page;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

@Component
public class TextRepository {

    private final ElasticsearchOperations elasticsearchTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public TextRepository(ElasticsearchOperations elasticsearchTemplate, ObjectMapper objectMapper) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.objectMapper = objectMapper;
    }

    public Page<?> search(String text, Pageable pageable) {
        QueryBuilder qb = multiMatchQuery(text)
                .field("number", 4)
                .field("title", 3)
                .field("body")
                .field("question")
                .field("answer")
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                .tieBreaker(0.3f);

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(qb)
                .withIndices("ftc-manuals-texts")
                .withPageable(pageable)
                .build();

        return elasticsearchTemplate.query(searchQuery, searchResponse -> {
            Page<Object> ret = new Page<>();
            searchResponse.getHits().iterator().forEachRemaining((hit) -> {
                Class<?> type;
                switch ((String) hit.getSourceAsMap().get("type")) {
                    case "Rule":
                        type = Rule.class;
                        break;
                    case "Definition":
                        type = Definition.class;
                        break;
                    case "ForumPost":
                        type = ForumPost.class;
                        break;
                    default:
                        //Unsupported type
                        //TODO log
                        return;
                }

                try {
                    ret.add(objectMapper.readValue(hit.getSourceAsString(), type));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            ret.setPageNumber(pageable.getPageNumber());
            long totalResults = searchResponse.getHits().totalHits;
            ret.setTotalSize(totalResults);
            ret.setTotalPageCount((totalResults + pageable.getPageSize() - 1) / pageable.getPageSize());
            return ret;
        });
    }
}
