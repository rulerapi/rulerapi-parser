package com.maths22.ftcmanuals.repositories.elasticsearch;

import com.maths22.ftcmanuals.models.Rule;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface RuleEsRepository extends ElasticsearchRepository<Rule, String> {
    // Relevant queries will be added
}
