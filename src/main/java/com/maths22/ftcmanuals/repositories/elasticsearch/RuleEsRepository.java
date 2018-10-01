package com.maths22.ftcmanuals.repositories.elasticsearch;

import com.maths22.ftcmanuals.models.Rule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface RuleEsRepository extends ElasticsearchRepository<Rule, String> {
    default Page<Rule> findAll(Pageable pageVar) {
        return findAllByType(Rule.class.getSimpleName(), pageVar);
    }

    Page<Rule> findAllByType(String type, Pageable pageVar);

    default void deleteAll() {
        deleteAllByType(Rule.class.getSimpleName());
    }

    void deleteAllByType(String type);
}
