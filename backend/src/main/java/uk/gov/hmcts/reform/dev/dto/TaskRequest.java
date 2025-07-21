package uk.gov.hmcts.reform.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dev.entity.Task.TaskStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    @NotBlank(message = "The task title is required.")
    @Schema(
        description = "The title of the task",
        example = "Review case documents",
        minLength = 1,
        maxLength = 255
    )
    private String title;

    @Schema(
        description = "Detailed description of the task",
        example = "Review all submitted documents for case #12345 and ensure compliance with regulations",
        nullable = true
    )
    private String description;

    @NotNull(message = "Task status must be one of TODO, IN_PROGRESS, COMPLETED, OR CANCELLED")
    @Schema(
        description = "Current status of the task",
        example = "TODO",
        allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED"}
    )
    private TaskStatus status;

    @Schema(
        description = "Due date and time for the task in UTC",
        example = "2024-12-31T17:00:00Z",
        nullable = true,
        pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    private LocalDateTime dueDateTime;
}
