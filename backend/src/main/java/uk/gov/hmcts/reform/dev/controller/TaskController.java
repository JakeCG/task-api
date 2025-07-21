package uk.gov.hmcts.reform.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.entity.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.service.TaskService;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {

    private final TaskService taskService;

    @Operation(
        summary = "Create a new task",
        description = "Creates a new task with the provided details. The task will be stored in the database"
            + " with an auto-generated ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Task created successfully",
            content = @Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TaskResponse.class)
                )
            ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class)
                )
            ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
            )
    })
    @PostMapping(
        value = "/create-task",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TaskResponse> createTask(
        @Parameter(description = "Task details", required = true)
        @Valid @RequestBody TaskRequest taskRequest) {
        log.info("Creating task: {}, with title: {}", taskRequest, taskRequest.getTitle());
        TaskResponse taskResponse = taskService.createTask(taskRequest);
        return ResponseEntity.status(CREATED).body(taskResponse);
    }

    @Operation(
        summary = "Get task by ID",
        description = "Retrieves a specific task by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Task retrieved successfully",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TaskResponse.class)
                )
            ),
        @ApiResponse(
                responseCode = "404",
                description = "Task not found"
            ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
    })
    @GetMapping(
        value = "/{id}/get-task",
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TaskResponse> getTaskById(
        @Parameter(description = "Task ID", example = "1", required = true)
        @PathVariable Long id) {
        log.info("Retrieving task with id: {}", id);
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get all tasks",
        description = "Retrieves all tasks ordered by creation date (newest first)"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Tasks retrieved successfully",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TaskResponse.class)
                )
            ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
    })
    @GetMapping(value = "/get-all-tasks", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        log.info("Retrieving all tasks");
        List<TaskResponse> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @Operation(
        summary = "Update task status",
        description = "Updates only the status of an existing task"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Task status updated successfully",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TaskResponse.class)
                )
            ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid status value",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            ),
        @ApiResponse(
                responseCode = "404",
                description = "Task not found"
            ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
    })
    @PatchMapping(
        value = "/{id}/status",
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TaskResponse> updateTaskStatus(
        @Parameter(description = "Task ID", example = "1", required = true)
        @PathVariable Long id,
        @Parameter(description = "New task status", required = true, schema =
            @Schema(allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED"}))
        @RequestParam TaskStatus status) {
        log.info("Updating task {} status to: {}", id, status);
        TaskResponse response = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update task",
        description = "Updates all fields of an existing task"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Task updated successfully",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TaskResponse.class)
                )
            ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            ),
        @ApiResponse(
                responseCode = "404",
                description = "Task not found"
            ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
    })
    @PutMapping(
        value = "/{id}/update-task",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TaskResponse> updateTask(
        @Parameter(description = "Task ID", example = "1", required = true)
        @PathVariable Long id,
        @Parameter(description = "Updated task details", required = true)
        @Valid @RequestBody TaskRequest request) {
        log.info("Updating task with id: {}", id);
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete task",
        description = "Permanently deletes a task from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "204",
                description = "Task deleted successfully"
            ),
        @ApiResponse(
                responseCode = "404",
                description = "Task not found"
            ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
    })
    @DeleteMapping("/{id}/delete-task")
    public ResponseEntity<Void> deleteTask(
        @Parameter(description = "Task ID", example = "1", required = true)
        @PathVariable Long id) {
        log.info("Deleting task with id: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
