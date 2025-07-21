package uk.gov.hmcts.reform.dev.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Schema(description = "Unique identifier of the task", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Title of the task", example = "Review case documents")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Description of the task", example = "Review all documents for case #12345")
    private String description;

    @Column(nullable = false)
    @Enumerated(STRING)
    @Schema(description = "Current status of the task", example = "IN_PROGRESS")
    private TaskStatus status;

    @Column(name = "due_date_time")
    @Schema(description = "Due date and time for task completion", example = "2024-09-03T15:30:45")
    private LocalDateTime dueDateTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Timestamp when the task was created", example = "2024-09-03T15:30:45")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Schema(description = "Timestamp when the task was last updated", example = "2024-09-03T15:30:45")
    private LocalDateTime updatedAt;

    @Schema(description = "Possible status values for a task")
    public enum TaskStatus {
        TODO,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
