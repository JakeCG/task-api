package uk.gov.hmcts.reform.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dev.entity.Task;
import uk.gov.hmcts.reform.dev.entity.Task.TaskStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing task details")
public class TaskResponse {
    @Schema(
        description = "Unique identifier of the task",
        example = "1"
    )
    private Long id;

    @Schema(
        description = "The title of the task",
        example = "Review case documents"
    )
    private String title;

    @Schema(
        description = "Detailed description of the task",
        example = "Review all submitted documents for case #12345 and ensure compliance with regulations",
        nullable = true
    )
    private String description;

    @Schema(
        description = "Current status of the task",
        example = "IN_PROGRESS"
    )
    private TaskStatus status;

    @Schema(
        description = "Due date and time for the task",
        example = "2024-12-31T17:00:00Z",
        nullable = true
    )
    private LocalDateTime dueDateTime;

    @Schema(
        description = "Timestamp when the task was created",
        example = "2024-12-31T17:00:00Z"
    )
    private LocalDateTime createdAt;

    @Schema(
        description = "Timestamp when the task was last updated",
        example = "2024-12-31T17:00:00Z"
    )
    private LocalDateTime updatedAt;

    public static TaskResponse fromEntity(Task task) {
        return TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .dueDateTime(task.getDueDateTime())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}
