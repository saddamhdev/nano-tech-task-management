package snvn.taskmanagementservice.dto.task;

import java.time.Instant;

public record CommentResponse(Long id, String author, String content, Instant createdAt) {
}

