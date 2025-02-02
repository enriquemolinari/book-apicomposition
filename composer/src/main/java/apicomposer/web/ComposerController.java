package apicomposer.web;

import apicomposer.impl.ResponseComposer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        return composeResponse(request, new HashMap<>());
    }

    @GetMapping("/movies/{id}/rate")
    public ResponseEntity<List<Map<String, Object>>> handleMovieRates(HttpServletRequest request,
                                                                      @PathVariable Long id) {
        var params = new HashMap<String, Object>();
        params.put("id", id);
        return composeResponse(request, params);
    }

    private ResponseEntity<List<Map<String, Object>>> composeResponse(HttpServletRequest request,
                                                                      Map<String, Object> params) {
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        return ResponseEntity.ok(composer
                .composeResponse(requestPath, requestMethod, params));
    }
}
