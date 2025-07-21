package uk.gov.hmcts.reform.dev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.entity.Task;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.COMPLETED;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.IN_PROGRESS;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.TODO;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Testcontainers
@DisplayName("TaskController Integration Tests")
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "resource"})
class TaskControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final TaskRepository taskRepository;

    @Autowired
    TaskControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, TaskRepository taskRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.taskRepository = taskRepository;
    }

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Nested
    @DisplayName("Create Task")
    class CreateTask {

        @Test
        @DisplayName("should create task successfully")
        void shouldCreateTaskSuccessfully() throws Exception {
            TaskRequest request = TaskRequest.builder()
                .title("Integration Test Task")
                .description("Test Description")
                .status(TODO)
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

            mockMvc.perform(post("/api/tasks/create-task")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.dueDateTime").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

            assertThat(taskRepository.count()).isEqualTo(1);
            Task savedTask = taskRepository.findAll().getFirst();
            assertThat(savedTask.getTitle()).isEqualTo("Integration Test Task");
        }

        @Test
        @DisplayName("should return 400 when title is blank")
        void shouldReturn400WhenTitleIsBlank() throws Exception {
            TaskRequest request = TaskRequest.builder()
                .title("")
                .status(TODO)
                .build();

            mockMvc.perform(post("/api/tasks/create-task")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(taskRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return 400 when status is null")
        void shouldReturn400WhenStatusIsNull() throws Exception {
            TaskRequest request = TaskRequest.builder()
                .title("Test Task")
                .status(null)
                .build();

            mockMvc.perform(post("/api/tasks/create-task")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(taskRepository.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Get Task By ID")
    class GetTaskById {

        @Test
        @DisplayName("should retrieve task when exists")
        void shouldRetrieveTaskWhenExists() throws Exception {
            Task task = taskRepository.save(Task.builder()
                                                .title("Test Task")
                                                .description("Test Description")
                                                .status(IN_PROGRESS)
                                                .dueDateTime(LocalDateTime.now().plusDays(3))
                                                .build());

            mockMvc.perform(get("/api/tasks/{id}/get-task", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("should return 404 when task not found")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            mockMvc.perform(get("/api/tasks/999/get-task"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get All Tasks")
    class GetAllTasks {

        @Test
        @DisplayName("should return all tasks ordered by creation date")
        void shouldReturnAllTasksOrderedByCreationDate() throws Exception {
            taskRepository.save(Task.builder()
                                    .title("First Task")
                                    .status(TODO)
                                    .build());

            taskRepository.save(Task.builder()
                                    .title("Second Task")
                                    .status(IN_PROGRESS)
                                    .build());

            taskRepository.save(Task.builder()
                                    .title("Third Task")
                                    .status(COMPLETED)
                                    .build());

            mockMvc.perform(get("/api/tasks/get-all-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("First Task"))
                .andExpect(jsonPath("$[1].title").value("Second Task"))
                .andExpect(jsonPath("$[2].title").value("Third Task"));
        }

        @Test
        @DisplayName("should return empty list when no tasks")
        void shouldReturnEmptyListWhenNoTasks() throws Exception {
            mockMvc.perform(get("/api/tasks/get-all-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Update Task Status")
    class UpdateTaskStatus {

        @Test
        @DisplayName("should update task status successfully")
        void shouldUpdateTaskStatusSuccessfully() throws Exception {
            Task task = taskRepository.save(Task.builder()
                                                .title("Test Task")
                                                .status(TODO)
                                                .build());

            mockMvc.perform(patch("/api/tasks/{id}/status", task.getId())
                                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

            Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
            assertThat(updatedTask.getStatus()).isEqualTo(COMPLETED);
        }

        @Test
        @DisplayName("should return 404 when task not found")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            mockMvc.perform(patch("/api/tasks/999/status")
                                .param("status", "COMPLETED"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when invalid status")
        void shouldReturn400WhenInvalidStatus() throws Exception {
            Task task = taskRepository.save(Task.builder()
                                                .title("Test Task")
                                                .status(TODO)
                                                .build());

            mockMvc.perform(patch("/api/tasks/{id}/status", task.getId())
                                .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Update Task")
    class UpdateTask {

        @Test
        @DisplayName("should update all task fields")
        void shouldUpdateAllTaskFields() throws Exception {
            Task task = taskRepository.save(Task.builder()
                                                .title("Original Title")
                                                .description("Original Description")
                                                .status(TODO)
                                                .dueDateTime(LocalDateTime.now().plusDays(7))
                                                .build());

            TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .status(IN_PROGRESS)
                .dueDateTime(LocalDateTime.now().plusDays(14))
                .build();

            mockMvc.perform(put("/api/tasks/{id}/update-task", task.getId())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

            Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
            assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
            assertThat(updatedTask.getDescription()).isEqualTo("Updated Description");
            assertThat(updatedTask.getStatus()).isEqualTo(IN_PROGRESS);
            assertThat(updatedTask.getUpdatedAt()).isAfter(updatedTask.getCreatedAt());
        }

        @Test
        @DisplayName("should return 404 when task not found")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Title")
                .status(IN_PROGRESS)
                .build();

            mockMvc.perform(put("/api/tasks/999/update-task")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            Task task = taskRepository.save(Task.builder()
                                                .title("Test Task")
                                                .status(TODO)
                                                .build());

            TaskRequest invalidRequest = TaskRequest.builder()
                .title("")
                .status(TODO)
                .build();

            mockMvc.perform(put("/api/tasks/{id}/update-task", task.getId())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Delete Task")
    class DeleteTask {

        @Test
        @DisplayName("should delete task successfully")
        void shouldDeleteTaskSuccessfully() throws Exception {
            Task task = taskRepository.save(Task.builder()
                                                .title("Task to Delete")
                                                .status(TODO)
                                                .build());

            mockMvc.perform(delete("/api/tasks/{id}/delete-task", task.getId()))
                .andExpect(status().isNoContent());

            assertThat(taskRepository.existsById(task.getId())).isFalse();
        }

        @Test
        @DisplayName("should return 404 when task not found")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            mockMvc.perform(delete("/api/tasks/999/delete-task"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("CORS Configuration")
    class CorsConfiguration {

        @Test
        @DisplayName("should handle CORS preflight request")
        void shouldHandleCorsPreflight() throws Exception {
            mockMvc.perform(options("/api/tasks/get-all-tasks")
                                .header("Origin", "http://localhost:3000")
                                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should include CORS headers in response")
        void shouldIncludeCorsHeadersInResponse() throws Exception {
            mockMvc.perform(get("/api/tasks/get-all-tasks")
                                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
        }
    }
}
