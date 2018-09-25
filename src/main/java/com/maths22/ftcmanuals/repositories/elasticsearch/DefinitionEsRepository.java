package com.maths22.ftcmanuals.repositories.elasticsearch;

import com.maths22.ftcmanuals.models.Definition;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface DefinitionEsRepository extends ElasticsearchRepository<Definition, String> {
    default Iterable<Definition> findAll(Sort sortVar) {
        return findAllByType(Definition.class.getSimpleName(), sortVar);
    }

    Iterable<Definition> findAllByType(String type, Sort sortVar);
}
