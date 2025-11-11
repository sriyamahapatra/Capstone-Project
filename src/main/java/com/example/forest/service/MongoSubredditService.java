package com.example.forest.service;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.document.MongoPostDocument;
import com.example.forest.document.MongoSubredditDocument;
import com.example.forest.document.MongoUserDocument;
import com.example.forest.dto.SubredditDto;
import com.example.forest.mapper.MongoSubredditMapper;
import com.example.forest.model.Role;
import com.example.forest.repository.mongodb.MongoCommentRepository;
import com.example.forest.repository.mongodb.MongoPostRepository;
import com.example.forest.repository.mongodb.MongoSubredditRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Service layer responsible for managing subreddits in the Forest application.
 *
 * Handles CRUD operations for subreddits, ensuring:
 *  - Role-based authorization for updates and deletions
 *  - Cascade deletion of posts and comments when a subreddit is removed
 *  - Mapping between MongoDB entities and DTOs
 */
@Service
@AllArgsConstructor
@Slf4j
public class MongoSubredditService {

    private final MongoSubredditRepository subredditRepository;
    private final MongoSubredditMapper subredditMapper;
    private final AuthService authService;
    private final MongoPostRepository postRepository;
    private final MongoCommentRepository commentRepository;

    /**
     * Creates a new subreddit and assigns the current authenticated user as its owner.
     *
     * @param subredditDto DTO containing subreddit details (name, description, etc.)
     * @return Saved SubredditDto with generated ID.
     */
    @Transactional
    public SubredditDto save(SubredditDto subredditDto) {
        MongoUserDocument currentUser = authService.getCurrentUser();

        // Convert DTO to entity and save
        MongoSubredditDocument subreddit = subredditMapper.mapDtoToSubreddit(subredditDto);
        subreddit.setUser(currentUser);
        subredditRepository.save(subreddit);

        subredditDto.setId(subreddit.getId());
        log.info("Subreddit '{}' created by user '{}'", subreddit.getName(), currentUser.getUsername());
        return subredditDto;
    }

    /**
     * Retrieves all subreddits from the database.
     *
     * @return A list of SubredditDto objects representing all subreddits.
     */
    @Transactional(readOnly = true)
    public List<SubredditDto> getAll() {
        List<SubredditDto> subreddits = subredditRepository.findAll()
                .stream()
                .map(subredditMapper::mapSubredditToDto)
                .collect(toList());

        log.info("Fetched {} subreddits from database", subreddits.size());
        return subreddits;
    }

    /**
     * Retrieves a subreddit by its ID.
     *
     * @param id The unique ID of the subreddit.
     * @return SubredditDto containing subreddit details.
     */
    @Transactional(readOnly = true)
    public SubredditDto getSubreddit(String id) {
        MongoSubredditDocument subreddit = subredditRepository.findById(id)
                .orElseThrow(() -> new CustomException("No subreddit found with the given ID."));
        log.info("Fetched subreddit '{}' with ID: {}", subreddit.getName(), id);
        return subredditMapper.mapSubredditToDto(subreddit);
    }

    /**
     * Updates the name and description of a subreddit.
     *
     * Only the subreddit creator or an admin can perform this action.
     *
     * @param subredditDto DTO containing updated subreddit details.
     * @return Updated SubredditDto.
     */
    @Transactional
    public SubredditDto update(SubredditDto subredditDto) {
        MongoUserDocument currentUser = authService.getCurrentUser();

        MongoSubredditDocument subreddit = subredditRepository.findById(subredditDto.getId())
                .orElseThrow(() -> new CustomException("Subreddit doesn't exist!"));

        // Authorization check: admin or subreddit owner
        if (currentUser.getRole().equals(Role.ADMIN) || subreddit.getUser().equals(currentUser)) {
            subreddit.setName(subredditDto.getName());
            subreddit.setDescription(subredditDto.getDescription());
            subredditRepository.save(subreddit);

            log.info("Subreddit '{}' updated by user '{}'", subreddit.getName(), currentUser.getUsername());
            return subredditMapper.mapSubredditToDto(subreddit);
        } else {
            log.warn("User '{}' attempted to update subreddit '{}' without permission.",
                    currentUser.getUsername(), subreddit.getName());
            throw new CustomException("Cannot update subreddit: insufficient privileges.");
        }
    }

    /**
     * Deletes a subreddit and all its related posts and comments.
     *
     * Only the subreddit creator or an admin can perform this operation.
     * Ensures cascade cleanup of related data.
     *
     * @param id The ID of the subreddit to delete.
     */
    @Transactional
    public void delete(String id) {
        MongoUserDocument currentUser = authService.getCurrentUser();
        MongoSubredditDocument subreddit = subredditRepository.findById(id)
                .orElseThrow(() -> new CustomException("Subreddit doesn't exist!"));

        // Authorization check: admin or subreddit owner
        if (currentUser.getRole().equals(Role.ADMIN) || subreddit.getUser().equals(currentUser)) {
            log.info("Deleting subreddit '{}' by user '{}'", subreddit.getName(), currentUser.getUsername());

            // Fetch all posts under this subreddit
            List<MongoPostDocument> posts = postRepository.findAllBySubreddit(subreddit);

            // Delete all associated comments first
            for (MongoPostDocument post : posts) {
                commentRepository.deleteAllByPost(post);
            }

            // Then delete the posts themselves
            postRepository.deleteAll(posts);

            // Finally, delete the subreddit
            subredditRepository.delete(subreddit);
            log.info("Subreddit '{}' and all related posts/comments deleted successfully.", subreddit.getName());
        } else {
            log.warn("User '{}' attempted to delete subreddit '{}' without permission.",
                    currentUser.getUsername(), subreddit.getName());
            throw new CustomException("Cannot delete subreddit: insufficient privileges.");
        }
    }
}
