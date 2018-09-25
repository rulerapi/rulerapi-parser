package com.maths22.ftcmanuals.repositories.elasticsearch;

import com.maths22.ftcmanuals.models.Definition;
import com.maths22.ftcmanuals.models.ForumPost;
import com.maths22.ftcmanuals.models.Rule;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface RuleEsRepository extends ElasticsearchRepository<Rule, String> {
    default Iterable<Rule> findAll(Sort sortVar) {
        return findAllByType(Rule.class.getSimpleName(), sortVar);
    }

    Iterable<Rule> findAllByType(String type, Sort sortVar);
}
