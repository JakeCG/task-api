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
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.service.TaskService;

import static org.springframework.http.HttpStatus.CREATED;

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
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TaskResponse.class)
                )
            ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class)
                )
            ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class)
                )
            )
    })
    @PostMapping(
        value = "/create-task",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TaskResponse> createTask(
        @Parameter(description = "Task details", required = true)
        @Valid @RequestBody TaskRequest taskRequest
    ) {
        log.info("Creating task: {}, with title: {}", taskRequest, taskRequest.getTitle());
        TaskResponse taskResponse = taskService.createTask(taskRequest);
        return ResponseEntity.status(CREATED).body(taskResponse);
    }


}
