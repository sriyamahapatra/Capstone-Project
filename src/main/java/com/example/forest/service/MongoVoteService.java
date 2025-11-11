package com.example.forest.service;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.document.MongoPostDocument;
import com.example.forest.document.MongoUserDocument;
import com.example.forest.document.MongoVoteDocument;
import com.example.forest.dto.VoteDto;
import com.example.forest.model.VoteType;
import com.example.forest.repository.mongodb.MongoPostRepository;
import com.example.forest.repository.mongodb.MongoVoteRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for handling voting logic on posts.
 *
 * Responsibilities:
 *  - Manage upvotes/downvotes on posts.
 *  - Handle undoing and switching votes.
 *  - Maintain accurate post vote counts.
 *
 * The system ensures that:
 *  - Users can toggle their votes.
 *  - Vote counts remain consistent during updates.
 */
@Service
@AllArgsConstructor
@Slf4j
public class MongoVoteService {

    private final MongoVoteRepository voteRepository;
    private final MongoPostRepository postRepository;
    private final AuthService authService;

    /**
     * Handles voting on a post — supports upvote, downvote, undo, and switch.
     *
     * @param voteDto Contains the post ID and vote type (UPVOTE or DOWNVOTE).
     */
    @Transactional
    public void vote(VoteDto voteDto) {
        // Fetch target post
        MongoPostDocument post = postRepository.findById(voteDto.getPostId())
                .orElseThrow(() -> new CustomException("No posts found with id: " + voteDto.getPostId()));

        // Get currently authenticated user
        MongoUserDocument currentUser = authService.getCurrentUser();

        // Check if the user has already voted on this post
        Optional<MongoVoteDocument> existingVote =
                voteRepository.findTopByPostAndUserOrderByIdDesc(post, currentUser);

        if (existingVote.isPresent()) {
            handleExistingVote(voteDto, post, currentUser, existingVote.get());
        } else {
            // User is voting for the first time on this post
            applyNewVote(voteDto, post, currentUser);
        }

        // Persist post's new vote count
        postRepository.save(post);
    }

    /**
     * Handles vote changes for users who have already voted before.
     * Supports toggling or switching between UPVOTE and DOWNVOTE.
     */
    private void handleExistingVote(VoteDto voteDto, MongoPostDocument post,
                                    MongoUserDocument currentUser, MongoVoteDocument existingVote) {

        VoteType previousVote = existingVote.getVoteType();
        VoteType newVote = voteDto.getVoteType();

        // Case 1: Undo same vote
        if (previousVote == newVote) {
            adjustVoteCount(post, newVote, -1);
            voteRepository.deleteById(existingVote.getId());
            log.info("User '{}' undid their {} on post '{}'",
                    currentUser.getUsername(), newVote, post.getPostName());
            return;
        }

        // Case 2: Switch from upvote to downvote or vice versa
        int adjustment = (newVote == VoteType.UPVOTE) ? +2 : -2;
        post.setVoteCount(post.getVoteCount() + adjustment);

        // Replace previous vote record
        voteRepository.deleteById(existingVote.getId());
        voteRepository.save(mapVote(voteDto, post, currentUser));

        log.info("User '{}' switched vote from {} to {} on post '{}'",
                currentUser.getUsername(), previousVote, newVote, post.getPostName());
    }

    /**
     * Applies a brand new vote (no previous record found).
     */
    private void applyNewVote(VoteDto voteDto, MongoPostDocument post, MongoUserDocument currentUser) {
        adjustVoteCount(post, voteDto.getVoteType(), 1);
        voteRepository.save(mapVote(voteDto, post, currentUser));

        log.info("User '{}' casted a new {} on post '{}'",
                currentUser.getUsername(), voteDto.getVoteType(), post.getPostName());
    }

    /**
     * Adjusts the post's vote count based on the vote type and multiplier.
     *
     * @param post     The post being voted on.
     * @param voteType The type of vote (UPVOTE or DOWNVOTE).
     * @param factor   +1 for new vote, -1 for undo.
     */
    private void adjustVoteCount(MongoPostDocument post, VoteType voteType, int factor) {
        int adjustment = (voteType == VoteType.UPVOTE) ? +1 : -1;
        post.setVoteCount(post.getVoteCount() + (adjustment * factor));
    }

    /**
     * Maps a VoteDto to a MongoVoteDocument for persistence.
     *
     * @param voteDto Vote information (type, post ID)
     * @param post    The post being voted on.
     * @param user    The user casting the vote.
     * @return A new MongoVoteDocument instance.
     */
    private MongoVoteDocument mapVote(VoteDto voteDto, MongoPostDocument post, MongoUserDocument user) {
        return MongoVoteDocument.builder()
                .voteType(voteDto.getVoteType())
                .post(post)
                .user(user)
                .build();
    }
}
