package uk.gov.hmcts.reform.dev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.entity.Task;
import uk.gov.hmcts.reform.dev.entity.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.exception.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.COMPLETED;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.IN_PROGRESS;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.TODO;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImplementation Tests")
class TaskServiceImplementationTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImplementation taskService;

    private Task testTask;
    private TaskRequest testRequest;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = now();
        testTask = Task.builder()
            .id(1L)
            .title("Test Task")
            .description("Test Description")
            .status(TODO)
            .dueDateTime(testDateTime.plusDays(7))
            .createdAt(testDateTime)
            .updatedAt(testDateTime)
            .build();

        testRequest = TaskRequest.builder()
            .title("Test Task")
            .description("Test Description")
            .status(TODO)
            .dueDateTime(testDateTime.plusDays(7))
            .build();
    }

    @Nested
    @DisplayName("Create Task")
    class CreateTask {

        @Test
        @DisplayName("should create task successfully")
        void shouldCreateTaskSuccessfully() {
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            TaskResponse response = taskService.createTask(testRequest);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Test Task");
            assertThat(response.getDescription()).isEqualTo("Test Description");
            assertThat(response.getStatus()).isEqualTo(TODO);
            assertThat(response.getDueDateTime()).isEqualTo(testDateTime.plusDays(7));

            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            TaskRequest requestWithNulls = TaskRequest.builder()
                .title("Test Task")
                .description(null)
                .status(TODO)
                .dueDateTime(null)
                .build();

            Task taskWithNulls = Task.builder()
                .id(2L)
                .title("Test Task")
                .description(null)
                .status(TODO)
                .dueDateTime(null)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

            when(taskRepository.save(any(Task.class))).thenReturn(taskWithNulls);

            TaskResponse response = taskService.createTask(requestWithNulls);

            assertThat(response.getDescription()).isNull();
            assertThat(response.getDueDateTime()).isNull();
            assertThat(response.getTitle()).isEqualTo("Test Task");
        }

        @ParameterizedTest(name = "should create task with status {0}")
        @EnumSource(TaskStatus.class)
        @DisplayName("should create task with different statuses")
        void shouldCreateTaskWithDifferentStatuses(TaskStatus status) {
            testRequest.setStatus(status);
            testTask.setStatus(status);
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            TaskResponse response = taskService.createTask(testRequest);

            assertThat(response.getStatus()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("Get Task By ID")
    class GetTaskById {

        @Test
        @DisplayName("should return task when found")
        void shouldReturnTaskWhenFound() {
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

            TaskResponse response = taskService.getTaskById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Test Task");
            verify(taskRepository).findById(1L);
        }

        @ParameterizedTest(name = "should throw exception when task with id {0} not found")
        @CsvSource({"1", "99", "999", "0", "-1"})
        @DisplayName("should throw exception for non-existent tasks")
        void shouldThrowExceptionWhenTaskNotFound(Long id) {
            when(taskRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTaskById(id))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: " + id);

            verify(taskRepository).findById(id);
        }
    }

    @Nested
    @DisplayName("Get All Tasks")
    class GetAllTasks {

        @Test
        @DisplayName("should return all tasks ordered by created date")
        void shouldReturnAllTasksOrdered() {
            Task task1 = createTask(1L, "Task 1", TODO);
            Task task2 = createTask(2L, "Task 2", IN_PROGRESS);
            Task task3 = createTask(3L, "Task 3", COMPLETED);

            when(taskRepository.findAllByOrderByCreatedAtAsc())
                .thenReturn(List.of(task1, task2, task3));

            List<TaskResponse> responses = taskService.getAllTasks();

            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getTitle()).isEqualTo("Task 1");
            assertThat(responses.get(1).getTitle()).isEqualTo("Task 2");
            assertThat(responses.get(2).getTitle()).isEqualTo("Task 3");
            verify(taskRepository).findAllByOrderByCreatedAtAsc();
        }

        @Test
        @DisplayName("should return empty list when no tasks exist")
        void shouldReturnEmptyListWhenNoTasks() {
            when(taskRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of());

            List<TaskResponse> responses = taskService.getAllTasks();

            assertThat(responses).isEmpty();
            verify(taskRepository).findAllByOrderByCreatedAtAsc();
        }
    }

    @Nested
    @DisplayName("Update Task Status")
    class UpdateTaskStatus {

        @ParameterizedTest(name = "should update status from {0} to {1}")
        @CsvSource({
            "TODO, IN_PROGRESS",
            "IN_PROGRESS, COMPLETED",
            "COMPLETED, TODO",
            "TODO, CANCELLED"
        })
        @DisplayName("should update task status successfully")
        void shouldUpdateTaskStatusSuccessfully(TaskStatus fromStatus, TaskStatus toStatus) {
            testTask.setStatus(fromStatus);
            Task updatedTask = createTask(1L, "Test Task", toStatus);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

            TaskResponse response = taskService.updateTaskStatus(1L, toStatus);

            assertThat(response.getStatus()).isEqualTo(toStatus);
            verify(taskRepository).findById(1L);
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            when(taskRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTaskStatus(99L, COMPLETED))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: 99");

            verify(taskRepository).findById(99L);
            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Task")
    class UpdateTask {

        @Test
        @DisplayName("should update all task fields successfully")
        void shouldUpdateAllTaskFieldsSuccessfully() {
            TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .status(COMPLETED)
                .dueDateTime(testDateTime.plusDays(14))
                .build();

            Task updatedTask = Task.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated Description")
                .status(COMPLETED)
                .dueDateTime(testDateTime.plusDays(14))
                .createdAt(testDateTime)
                .updatedAt(now())
                .build();

            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

            TaskResponse response = taskService.updateTask(1L, updateRequest);

            assertThat(response.getTitle()).isEqualTo("Updated Title");
            assertThat(response.getDescription()).isEqualTo("Updated Description");
            assertThat(response.getStatus()).isEqualTo(COMPLETED);
            assertThat(response.getDueDateTime()).isEqualTo(testDateTime.plusDays(14));

            verify(taskRepository).findById(1L);
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("should update task with null optional fields")
        void shouldUpdateTaskWithNullOptionalFields() {
            TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Title")
                .description(null)
                .status(IN_PROGRESS)
                .dueDateTime(null)
                .build();

            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            taskService.updateTask(1L, updateRequest);

            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            when(taskRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTask(99L, testRequest))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: 99");

            verify(taskRepository).findById(99L);
            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Task")
    class DeleteTask {

        @Test
        @DisplayName("should delete task successfully when exists")
        void shouldDeleteTaskSuccessfully() {
            when(taskRepository.existsById(1L)).thenReturn(true);

            taskService.deleteTask(1L);

            verify(taskRepository).existsById(1L);
            verify(taskRepository).deleteById(1L);
        }

        @ParameterizedTest(name = "should throw exception when task with id {0} does not exist")
        @CsvSource({"1", "99", "999"})
        @DisplayName("should throw exception for non-existent tasks")
        void shouldThrowExceptionWhenTaskDoesNotExist(Long id) {
            when(taskRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> taskService.deleteTask(id))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: " + id);

            verify(taskRepository).existsById(id);
            verify(taskRepository, never()).deleteById(anyLong());
        }
    }

    private Task createTask(Long id, String title, TaskStatus status) {
        return Task.builder()
            .id(id)
            .title(title)
            .description("Description for " + title)
            .status(status)
            .dueDateTime(testDateTime.plusDays(7))
            .createdAt(testDateTime)
            .updatedAt(testDateTime)
            .build();
    }
}
