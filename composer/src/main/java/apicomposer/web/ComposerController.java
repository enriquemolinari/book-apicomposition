package apicomposer.web;

import apicomposer.impl.ResponseComposer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/composed")
public class ComposerController {

    private final ResponseComposer composer;

    public ComposerController(ResponseComposer composer) {
        this.composer = composer;
    }

    @GetMapping("/shows")
    public ResponseEntity<List<Map<String, Object>>> handleComposeShows(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        return
                ResponseEntity.ok(composer
                        .composeResponse(requestPath, requestMethod, new HashMap<>()));
    }
}
