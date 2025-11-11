package com.example.forest.service;

import com.example.forest.document.MongoPostDocument;
import com.example.forest.dto.PostResponse;
import com.example.forest.mapper.MongoPostMapper;
import com.example.forest.repository.mongodb.MongoPostRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for retrieving trending posts within a recent time window.
 * <p>
 * Current strategy:
 * - Consider posts created in the last 24 hours
 * - Sort by voteCount in descending order
 * - Map to DTOs for API consumption
 */
@Service
@AllArgsConstructor
public class TrendingService {

    private final MongoPostRepository postRepository; // kept for potential future use
    private final MongoPostMapper postMapper;
    private final MongoTemplate mongoTemplate;

    /**
     * Returns posts created within the last 24 hours, ordered by vote count (highest first).
     *
     * @return list of trending posts as {@link PostResponse}
     */
    public List<PostResponse> getTrendingPosts() {
        // Calculate the cutoff timestamp (now - 24 hours)
        Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);

        // Find posts with createdDate >= cutoff
        Query query = new Query()
                .addCriteria(Criteria.where("createdDate").gte(twentyFourHoursAgo))
                // Sort by voteCount descending to surface the most upvoted content first
                .with(Sort.by(Sort.Direction.DESC, "voteCount"));

        // Execute query and map domain objects to DTOs
        List<MongoPostDocument> trendingPosts = mongoTemplate.find(query, MongoPostDocument.class);

        return trendingPosts.stream()
                .map(postMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
