package com.example.forest.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * ContentModerationService.java
 *
 * Service responsible for moderating both text and media content
 * submitted by users to ensure compliance with Forest community guidelines.
 * <p>
 * It uses Google's **Gemini API** through Vertex AI for AI-driven moderation,
 * classifying user submissions as either `SAFE` or `UNSAFE` according to
 * pre-defined ethical, legal, and behavioral standards.
 */
@Service
public class ContentModerationService {

    /** The Google Cloud project ID injected from environment variables. */
    @Value("${google.cloud.project.id}")
    private String projectId;

    /** The AI model used for content moderation. */
    private static final String MODEL_NAME = "gemini-2.5-flash";

    /**
     * A comprehensive set of moderation rules defining acceptable and prohibited
     * behaviors and content types across the Forest platform.
     * <p>
     * This prompt is used as the base context for the AI moderation call.
     */
    private static final String GUIDELINES_PROMPT = """
        You are a content moderator for a social media platform called Forest. Your job is to determine if a user's post violates the community guidelines.

        Here are the platform-wide rules:

        --- START OF GUIDELINES ---
        Rule 1: Remember the Human (Zero Tolerance for Hate and Harassment)
        Forest is a place for creating community and belonging, not for attacking or diminishing others. Everyone has a right to use Forest free of harassment, bullying, and threats of violence. Communities and users that incite violence or that promote hate based on identity, vulnerability, or protected groups will be immediately and permanently banned.

        Rule 2: Keep it Legal and Legitimate
        Do not post illegal content, and do not solicit or facilitate illegal or prohibited transactions. This includes, but is not limited to, the promotion of fraud, illegal drugs, or pirated media.

        Rule 3: No Sexual, Abusive, or Aggressive Content
        We strictly enforce a ban on high-risk harmful content. A-Content: Do not post or encourage the sharing of abusive or aggressively violent content. P-Content: Never post or threaten to post intimate, sexually-explicit, or suggestive content of someone without their consent. Any pornographic content is prohibited. Minors: Do not share or encourage the sharing of any sexual, abusive, or suggestive content involving minors. Any predatory behavior toward minors is strictly prohibited and will be reported to law enforcement.

        Rule 4: Respect Privacy and Confidentiality
        Instigating harassment, for example by revealing someone’s personal or confidential information (doxxing), is not allowed. Be mindful of the personal data you share and the data of others.

        Rule 5: Participate Authentically
        Engage in communities where you have a genuine personal interest. Do not spam or engage in disruptive behaviors (including mass content manipulation) that interfere with other communities on Forest. Do not intentionally mislead others or impersonate an individual or entity in a deceptive manner.

        Rule 6: Abide by Community Rules and Moderation
        The culture of each community is shaped by its dedicated moderators. You must abide by the specific rules of the communities in which you participate and do not interfere with the moderation or function of communities in which you are not a member.

        Rule 7: Don't Break the Site
        Do not attempt to break, exploit, or interfere with the normal, intended use of the Forest platform or its underlying infrastructure.
        --- END OF GUIDELINES ---

        Now, analyze the following user-submitted post content. Based on the rules provided, does this post violate any of the community guidelines? Please respond with only one word: SAFE or UNSAFE.
        """;

    /**
     * Moderates a text post by checking it against the community guidelines.
     *
     * @param text the user-generated post text.
     * @return {@code true} if the content is flagged as inappropriate, otherwise {@code false}.
     */
    public boolean isContentInappropriate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false; // Empty content is treated as safe
        }
        return isContentInappropriate(ContentMaker.fromMultiModalData(
                GUIDELINES_PROMPT,
                "User Post Content:\n" + text
        ));
    }

    /**
     * Moderates an uploaded media file (image, video, etc.) using Gemini AI.
     *
     * @param media    the media binary data.
     * @param mimeType the MIME type (e.g., image/jpeg, video/mp4).
     * @return {@code true} if the content is flagged as inappropriate, otherwise {@code false}.
     */
    public boolean isContentInappropriate(Binary media, String mimeType) {
        if (media == null) {
            return false; // No media provided
        }
        return isContentInappropriate(ContentMaker.fromMultiModalData(
                GUIDELINES_PROMPT,
                PartMaker.fromMimeTypeAndData(mimeType, media.getData())
        ));
    }

    /**
     * Calls the Gemini API through Vertex AI to classify content as SAFE or UNSAFE.
     *
     * @param content the {@link Content} object representing text or media to analyze.
     * @return {@code true} if the model classifies the content as UNSAFE.
     */
    private boolean isContentInappropriate(Content content) {
        String location = "us-central1";

        try (VertexAI vertexAi = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAi);
            GenerateContentResponse response = model.generateContent(content);

            // Extract and normalize the model’s single-word classification
            String modelResponse = ResponseHandler.getText(response).trim();

            // Return true only if model marks it unsafe
            return "UNSAFE".equalsIgnoreCase(modelResponse);

        } catch (IOException e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            // Failsafe: block content if moderation service fails
            return true;
        }
    }
}
