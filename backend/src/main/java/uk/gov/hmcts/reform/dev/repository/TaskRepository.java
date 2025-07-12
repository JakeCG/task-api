package uk.gov.hmcts.reform.dev.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dev.entity.Task;

import java.util.List;

@Repository
public interface TaskRepository {
    List<Task> findAllByOrderCreatedAt();
}
