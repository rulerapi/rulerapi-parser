package com.maths22.ftcmanuals.repositories.elasticsearch;

import com.maths22.ftcmanuals.models.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface ForumPostEsRepository extends ElasticsearchRepository<ForumPost, String> {
    default Page<ForumPost> findAll(Pageable pageVar) {
        return findAllByType(ForumPost.class.getSimpleName(), pageVar);
    }

    Page<ForumPost> findAllByType(String type, Pageable pageVar);

    default void deleteAll() {
        deleteAllByType(ForumPost.class.getSimpleName());
    }

    void deleteAllByType(String type);
}
