package com.stackclone.dto;

import com.stackclone.dto.UserDtos.UserSummary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class PostDtos {

    private PostDtos() {
    }

    public record QuestionSummary(Long id, String title, String excerpt, int score, int answerCount,
                                  int viewCount, boolean answered, List<String> tags,
                                  UserSummary author, Instant createdAt) {
    }

    public record QuestionDetail(Long id, String title, String body, int score, int viewCount, String status,
                                 List<String> tags, UserSummary author, Instant createdAt, Instant updatedAt,
                                 int myVote, Long acceptedAnswerId,
                                 List<CommentDto> comments, List<AnswerDto> answers) {
    }

    public record AnswerDto(Long id, Long questionId, String body, int score, boolean accepted,
                            UserSummary author, Instant createdAt, Instant updatedAt, int myVote,
                            List<CommentDto> comments) {
    }

    public record CommentDto(Long id, Long postId, String body, UserSummary author, Instant createdAt) {
    }

    public record QuestionRequest(
            @NotBlank @Size(min = 15, max = 150) String title,
            @NotBlank @Size(min = 30, max = 30000) String body,
            @NotEmpty @Size(max = 5, message = "5 tags maximum")
            List<@Pattern(regexp = "^[a-zA-Z0-9+#.-]{1,35}$", message = "tag invalide") String> tags) {
    }

    public record AnswerRequest(@NotBlank @Size(min = 20, max = 30000) String body) {
    }

    public record CommentRequest(@NotBlank @Size(min = 5, max = 600) String body) {
    }
}
