package uk.gov.hmcts.reform.dev.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.dev.entity.Task;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.IN_PROGRESS;

@DisplayName("TaskResponse Tests")
class TaskResponseTest {

    @Test
    @DisplayName("should map all fields from entity correctly")
    void shouldMapAllFieldsFromEntity() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(7);

        Task task = Task.builder()
            .id(1L)
            .title("Test Task")
            .description("Test Description")
            .status(IN_PROGRESS)
            .dueDateTime(dueDate)
            .createdAt(now)
            .updatedAt(now)
            .build();

        TaskResponse response = TaskResponse.fromEntity(task);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Task");
        assertThat(response.getDescription()).isEqualTo("Test Description");
        assertThat(response.getStatus()).isEqualTo(IN_PROGRESS);
        assertThat(response.getDueDateTime()).isEqualTo(dueDate);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("should handle null optional fields")
    void shouldHandleNullOptionalFields() {
        LocalDateTime now = LocalDateTime.now();

        Task task = Task.builder()
            .id(2L)
            .title("Test Task")
            .description(null)
            .status(IN_PROGRESS)
            .dueDateTime(null)
            .createdAt(now)
            .updatedAt(now)
            .build();

        TaskResponse response = TaskResponse.fromEntity(task);

        assertThat(response.getDescription()).isNull();
        assertThat(response.getDueDateTime()).isNull();
        assertThat(response.getTitle()).isEqualTo("Test Task");
        assertThat(response.getStatus()).isEqualTo(IN_PROGRESS);
    }
}
