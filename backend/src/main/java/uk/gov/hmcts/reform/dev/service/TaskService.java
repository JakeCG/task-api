package uk.gov.hmcts.reform.dev.service;

import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.entity.Task.TaskStatus;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(TaskRequest taskRequest);

    TaskResponse getTaskById(Long taskId);

    List<TaskResponse> getAllTasks();

    TaskResponse updateTaskStatus(Long taskId, TaskStatus taskStatus);

    TaskResponse updateTask(Long taskId, TaskRequest taskRequest);

    void deleteTask(Long taskId);
}
