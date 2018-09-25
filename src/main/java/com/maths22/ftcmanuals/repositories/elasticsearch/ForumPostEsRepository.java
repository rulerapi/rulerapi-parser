package com.maths22.ftcmanuals.repositories.elasticsearch;

import com.maths22.ftcmanuals.models.Definition;
import com.maths22.ftcmanuals.models.ForumPost;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ForumPostEsRepository extends ElasticsearchRepository<ForumPost, String> {
    default Iterable<ForumPost> findAll(Sort sortVar) {
        return findAllByType(ForumPost.class.getSimpleName(), sortVar);
    }

    Iterable<ForumPost> findAllByType(String type, Sort sortVar);
}
