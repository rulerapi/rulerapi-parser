package com.maths22.ftcmanuals.repositories.elasticsearch;

import com.maths22.ftcmanuals.models.ForumPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ForumPostEsRepository extends ElasticsearchRepository<ForumPost, String> {
    // Relevant queries will be added
}
