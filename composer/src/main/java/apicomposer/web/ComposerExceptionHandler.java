package apicomposer.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class ComposerExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllMyException(Exception ex, WebRequest request) {
        //Stream error to operations log
        return new ResponseEntity<>(Map.of("message", "Ups... something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
