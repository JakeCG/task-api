package uk.gov.hmcts.reform.dev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.exception.GlobalExceptionHandler;
import uk.gov.hmcts.reform.dev.service.TaskService;

import java.util.List;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.CANCELLED;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.COMPLETED;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.IN_PROGRESS;
import static uk.gov.hmcts.reform.dev.entity.Task.TaskStatus.TODO;

@WebMvcTest({TaskController.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("TaskController Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Nested
    @DisplayName("Create Task")
    class CreateTask {

        @Test
        @DisplayName("should return created task with status 201")
        void shouldReturnCreatedTask() throws Exception {
            TaskRequest request = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TODO)
                .build();

            TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TODO)
                .createdAt(now())
                .updatedAt(now())
                .build();

            when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

            String result = mockMvc.perform(post("/api/tasks/create-task")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andReturn()
                .getResponse()
                .getContentAsString();

            TaskResponse actualResponse = objectMapper.readValue(result, TaskResponse.class);
            assertThat(actualResponse.getId()).isEqualTo(1L);
            assertThat(actualResponse.getTitle()).isEqualTo("Test Task");
            assertThat(actualResponse.getStatus()).isEqualTo(TODO);

            verify(taskService).createTask(any(TaskRequest.class));
        }

        @Test
        @DisplayName("should return bad request when data is invalid")
        void shouldReturnBadRequestWhenInvalidData() throws Exception {
            TaskRequest invalidRequest = TaskRequest.builder()
                .title("")
                .description("Test Description")
                .build();

            mockMvc.perform(post("/api/tasks/create-task")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get Tasks")
    class GetTasks {

        @Test
        @DisplayName("should return task by ID")
        void shouldReturnTaskById() throws Exception {
            TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TODO)
                .createdAt(now())
                .updatedAt(now())
                .build();

            when(taskService.getTaskById(1L)).thenReturn(response);

            String result = mockMvc.perform(get("/api/tasks/1/get-task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andReturn()
                .getResponse()
                .getContentAsString();

            TaskResponse actualResponse = objectMapper.readValue(result, TaskResponse.class);
            assertThat(actualResponse.getId()).isEqualTo(1L);
            assertThat(actualResponse.getTitle()).isEqualTo("Test Task");

            verify(taskService).getTaskById(1L);
        }

        @Test
        @DisplayName("should return all tasks")
        void shouldReturnAllTasks() throws Exception {
            List<TaskResponse> tasks = List.of(
                TaskResponse.builder()
                    .id(1L)
                    .title("Task 1")
                    .status(TODO)
                    .createdAt(now())
                    .updatedAt(now())
                    .build(),
                TaskResponse.builder()
                    .id(2L)
                    .title("Task 2")
                    .status(CANCELLED)
                    .createdAt(now())
                    .updatedAt(now())
                    .build()
            );

            when(taskService.getAllTasks()).thenReturn(tasks);

            String result = mockMvc.perform(get("/api/tasks/get-all-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andReturn()
                .getResponse()
                .getContentAsString();

            List<TaskResponse> actualTasks = objectMapper.readValue(result,
                objectMapper.getTypeFactory().constructCollectionType(List.class, TaskResponse.class));

            assertThat(actualTasks).hasSize(2);
            assertThat(actualTasks.get(0).getId()).isEqualTo(1L);
            assertThat(actualTasks.get(1).getId()).isEqualTo(2L);

            verify(taskService).getAllTasks();
        }
    }

    @Nested
    @DisplayName("Update Task")
    class UpdateTask {

        @Test
        @DisplayName("should update task status")
        void shouldUpdateTaskStatus() throws Exception {
            TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .status(COMPLETED)
                .createdAt(now())
                .updatedAt(now())
                .build();

            when(taskService.updateTaskStatus(1L, COMPLETED)).thenReturn(response);

            String result = mockMvc.perform(patch("/api/tasks/1/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

            TaskResponse actualResponse = objectMapper.readValue(result, TaskResponse.class);
            assertThat(actualResponse.getId()).isEqualTo(1L);
            assertThat(actualResponse.getStatus()).isEqualTo(COMPLETED);

            verify(taskService).updateTaskStatus(1L, COMPLETED);
        }

        @Test
        @DisplayName("should update entire task")
        void shouldUpdateTask() throws Exception {
            TaskRequest request = TaskRequest.builder()
                .title("Updated Task")
                .description("Updated Description")
                .status(IN_PROGRESS)
                .build();

            TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Updated Task")
                .description("Updated Description")
                .status(IN_PROGRESS)
                .createdAt(now())
                .updatedAt(now())
                .build();

            when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(response);

            String result = mockMvc.perform(put("/api/tasks/1/update-task")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andReturn()
                .getResponse()
                .getContentAsString();

            TaskResponse actualResponse = objectMapper.readValue(result, TaskResponse.class);
            assertThat(actualResponse.getId()).isEqualTo(1L);
            assertThat(actualResponse.getTitle()).isEqualTo("Updated Task");

            verify(taskService).updateTask(eq(1L), any(TaskRequest.class));
        }

        @Test
        @DisplayName("should return bad request when update data is invalid")
        void shouldReturnBadRequestWhenUpdateDataIsInvalid() throws Exception {
            TaskRequest invalidRequest = TaskRequest.builder()
                .title("")
                .description("Description")
                .build();

            mockMvc.perform(put("/api/tasks/1/update-task")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Delete Task")
    class DeleteTask {

        @Test
        @DisplayName("should delete task and return no content")
        void shouldDeleteTaskAndReturnNoContent() throws Exception {
            mockMvc.perform(delete("/api/tasks/1/delete-task"))
                .andExpect(status().isNoContent());

            verify(taskService).deleteTask(1L);
        }
    }

    @Nested
    @DisplayName("CORS Configuration")
    class CorsConfiguration {

        @Test
        @DisplayName("should allow localhost origin")
        void shouldAllowLocalhostOrigin() throws Exception {
            when(taskService.getAllTasks()).thenReturn(List.of());

            mockMvc.perform(get("/api/tasks/get-all-tasks")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
        }
    }
}
