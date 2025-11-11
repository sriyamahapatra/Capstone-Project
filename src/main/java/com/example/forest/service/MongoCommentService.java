package com.example.forest.service;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.document.MongoCommentDocument;
import com.example.forest.document.MongoPostDocument;
import com.example.forest.document.MongoUserDocument;
import com.example.forest.dto.CommentsDto;
import com.example.forest.mapper.MongoCommentMapper;
import com.example.forest.model.NotificationEmail;
import com.example.forest.model.Role;
import com.example.forest.repository.mongodb.MongoCommentRepository;
import com.example.forest.repository.mongodb.MongoPostRepository;
import com.example.forest.repository.mongodb.MongoUserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing comments on posts.
 * Handles creation, retrieval, updating, and deletion of comments,
 * including notification handling when a user comments on a post.
 */
@Service
@AllArgsConstructor
@Slf4j
public class MongoCommentService {

    private final MongoCommentRepository commentRepository;
    private final AuthService authService;
    private final MongoUserRepository userRepository;
    private final MongoPostRepository postRepository;
    private final MongoCommentMapper commentMapper;
    private final MailService mailService;

    /**
     * Saves a new comment to the database and notifies the post owner (if notifications are enabled).
     *
     * @param commentsDto DTO containing comment details.
     */
    @Transactional
    public void save(CommentsDto commentsDto) {
        // Fetch the currently logged-in user
        MongoUserDocument user = authService.getCurrentUser();

        // Fetch the post associated with the comment
        MongoPostDocument post = postRepository.findById(commentsDto.getPostId())
                .orElseThrow(() -> new CustomException("No posts found with post id: " + commentsDto.getPostId()));

        // Map DTO to MongoDB document
        MongoCommentDocument comment = commentMapper.map(commentsDto, post, user);

        // Save the new comment
        commentRepository.save(comment);

        // Construct post URL (used in email notification)
        String POST_URL = "https://zealous-wave-027e5c910.3.azurestaticapps.net/#/view-post/" + commentsDto.getPostId();

        // Send notification email if the post owner has enabled comment notifications
        if (post.isNotificationStatus()) {
            String message = user.getUsername() +
                    " posted a response to your post. Click here to go to the post: " + POST_URL;
            sendCommentNotification(message, post.getUser(), user);
        }
    }

    /**
     * Retrieves all comments from the database.
     *
     * @return List of CommentsDto objects.
     */
    @Transactional(readOnly = true)
    public List<CommentsDto> getAllComments() {
        return commentRepository.findAll()
                .stream()
                .map(commentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Sends an email notification to the post owner when someone comments.
     *
     * @param message      Notification message content.
     * @param threadstarter The post creator (recipient).
     * @param commenter     The user who commented.
     */
    private void sendCommentNotification(String message, MongoUserDocument threadstarter, MongoUserDocument commenter) {
        mailService.sendMail(
                new NotificationEmail(
                        commenter.getUsername() + " replied to your post",
                        threadstarter.getEmail(),
                        threadstarter.getUsername(),
                        message
                )
        );
    }

    /**
     * Retrieves all comments for a specific post.
     *
     * @param postId ID of the post.
     * @return List of comments related to the post.
     */
    @Transactional(readOnly = true)
    public List<CommentsDto> getAllCommentsForPost(String postId) {
        MongoPostDocument post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("No posts found with post id: " + postId));

        return commentRepository.findByPost(post)
                .stream()
                .map(commentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all comments made by a specific user.
     *
     * @param userName Username whose comments should be retrieved.
     * @return List of comments authored by the given user.
     */
    @Transactional(readOnly = true)
    public List<CommentsDto> getAllCommentsForUser(String userName) {
        MongoUserDocument user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new CustomException("No user found with username: " + userName));

        return commentRepository.findAllByUser(user)
                .stream()
                .map(commentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing comment.
     * Only admins, moderators, or the comment owner can perform this action.
     *
     * @param commentsDto DTO containing the updated comment content.
     */
    @Transactional
    public void update(CommentsDto commentsDto) {
        MongoUserDocument user = authService.getCurrentUser();

        MongoCommentDocument comment = commentRepository.findById(commentsDto.getId())
                .orElseThrow(() -> new CustomException("Comment doesn't exist!"));

        // Authorization check
        if (user.getRole().equals(Role.ADMIN)
                || user.getRole().equals(Role.MODERATOR)
                || comment.getUser().equals(user)) {

            comment.setText(commentsDto.getText());
            commentRepository.save(comment);

        } else {
            throw new CustomException("Insufficient privileges to edit this comment!");
        }
    }

    /**
     * Deletes a comment from the database.
     * Only admins, moderators, or the comment owner can perform this action.
     *
     * @param commentsDto DTO identifying the comment to delete.
     */
    @Transactional
    public void delete(CommentsDto commentsDto) {
        MongoUserDocument user = authService.getCurrentUser();

        MongoCommentDocument comment = commentRepository.findById(commentsDto.getId())
                .orElseThrow(() -> new CustomException("Comment doesn't exist!"));

        // Authorization check
        if (user.getRole().equals(Role.ADMIN)
                || user.getRole().equals(Role.MODERATOR)
                || comment.getUser().equals(user)) {

            commentRepository.delete(comment);

        } else {
            throw new CustomException("Insufficient privileges to delete this comment!");
        }
    }
}
