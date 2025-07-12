package uk.gov.hmcts.reform.dev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dev.entity.Task;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    @NotBlank(message = "The task title is required.")
    private String title;

    private String description;

    @NotNull(message = "Task status must be one of TODO, IN_PROGRESS, COMPLETED, OR CANCELLED")
    private Task.TaskStatus taskStatus;

    private Instant dueDateTime;
}
