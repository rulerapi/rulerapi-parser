package com.maths22.ftcmanuals.repositories.elasticsearch;

import com.maths22.ftcmanuals.models.Definition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface DefinitionEsRepository extends ElasticsearchRepository<Definition, String> {
    default Page<Definition> findAll(Pageable pageVar) {
        return findAllByType(Definition.class.getSimpleName(), pageVar);
    }

    Page<Definition> findAllByType(String type, Pageable pageVar);

    default void deleteAll() {
        deleteAllByType(Definition.class.getSimpleName());
    }

    void deleteAllByType(String type);
}
