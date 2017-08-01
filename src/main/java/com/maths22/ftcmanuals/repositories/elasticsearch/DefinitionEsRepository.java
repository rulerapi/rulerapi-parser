package com.maths22.ftcmanuals.repositories.elasticsearch;

import com.maths22.ftcmanuals.models.Definition;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DefinitionEsRepository extends ElasticsearchRepository<Definition, String> {
    // Relevant queries will be added
}
