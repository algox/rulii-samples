package org.rulii.sample.web;

import org.rulii.validation.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {

    public ExceptionHandler() {
        super();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({ValidationException.class})
    ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ((ValidationException) ex).getViolations(),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}
