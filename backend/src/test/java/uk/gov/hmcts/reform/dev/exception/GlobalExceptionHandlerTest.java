package uk.gov.hmcts.reform.dev.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("should handle TaskNotFoundException")
    void shouldHandleTaskNotFoundException() {
        TaskNotFoundException ex = new TaskNotFoundException("Task not found with id: 123");

        ProblemDetail response = handler.handleTaskNotFoundException(ex);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getTitle()).isEqualTo("Task Not Found");
        assertThat(response.getDetail()).isEqualTo("Task not found with id: 123");
        assertThat(response.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("should handle validation exceptions")
    void shouldHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("taskRequest", "title", "The task title is required.");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ProblemDetail response = handler.handleValidationExceptions(ex);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getTitle()).isEqualTo("Validation Error");
        assertThat(response.getDetail()).isEqualTo("Validation failed");

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) Objects.requireNonNull(response.getProperties())
                                                                .get("errors");
        assertThat(errors).containsEntry("title", "The task title is required.");
    }

    @Test
    @DisplayName("should handle type mismatch exceptions")
    void shouldHandleTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("status");
        when(ex.getValue()).thenReturn("INVALID");

        ProblemDetail response = handler.handleTypeMismatchException(ex);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getTitle()).isEqualTo("Invalid Parameter");
        assertThat(response.getDetail()).isEqualTo("Invalid value 'INVALID' for parameter 'status'");
    }

    @Test
    @DisplayName("should handle illegal argument exceptions")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");

        ProblemDetail response = handler.handleIllegalArgumentException(ex);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getTitle()).isEqualTo("Invalid Request");
        assertThat(response.getDetail()).isEqualTo("Invalid argument provided");
    }

    @Test
    @DisplayName("should handle generic exceptions")
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");

        ProblemDetail response = handler.handleGenericException(ex);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getTitle()).isEqualTo("Internal Server Error");
        assertThat(response.getDetail()).isEqualTo("An unexpected error occurred");
    }
}
