package apicomposer.web;

import apicomposer.impl.ResponseComposer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/composed")
public class ComposerController {

    public static final String HEADER_USERID_NAME = "fw-gateway-user-id";
    private static final String AUTHENTICATION_REQUIRED = "Unauthorized";
    private final ResponseComposer composer;
    private final String userIdParamName;
    private final String movieIdParamName;

    public ComposerController(ResponseComposer composer,
                              @Value("${composer.userid.param.name}") String userIdParamName,
                              @Value("${composer.movieid.param.name}") String movieIdParamName) {
        this.composer = composer;
        this.userIdParamName = userIdParamName;
        this.movieIdParamName = movieIdParamName;
    }

    @GetMapping("/shows")
    public ResponseEntity<List<Map<String, Object>>> handleComposeShows(HttpServletRequest request) {
        return composeJsonResponseList(request, new HashMap<>());
    }

    @GetMapping("/movies/{id}/rate")
    public ResponseEntity<List<Map<String, Object>>> handleMovieRates(HttpServletRequest request,
                                                                      @PathVariable Long id) {
        var params = new HashMap<String, Object>();
        params.put(movieIdParamName, id);
        return composeJsonResponseList(request, params);
    }

    @GetMapping("/users/private/profile")
    public ResponseEntity<Map<String, Object>> handleUsersProfile(HttpServletRequest request,
                                                                  @RequestHeader(value = HEADER_USERID_NAME, required = false) Long id) {
        checkUserIdIsPresent(id);
        var params = new HashMap<String, Object>();
        params.put(userIdParamName, id);
        return composeJsonResponseObject(request, params);
    }

    private ResponseEntity<Map<String, Object>> composeJsonResponseObject(HttpServletRequest request,
                                                                          Map<String, Object> params) {
        var listOfMap = composeListOfMap(request, params);
        if (!listOfMap.isEmpty())
            return ResponseEntity.ok(listOfMap.getFirst());
        throw new RuntimeException("Empty response");
    }

    private ResponseEntity<List<Map<String, Object>>> composeJsonResponseList(HttpServletRequest request,
                                                                              Map<String, Object> params) {
        return ResponseEntity.ok(composeListOfMap(request, params));
    }

    private List<Map<String, Object>> composeListOfMap(HttpServletRequest request, Map<String, Object> params) {
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        return composer
                .composeResponse(requestPath, requestMethod, params);
    }

    private void checkUserIdIsPresent(Long id) {
        if (id == null) {
            throw new AuthException(AUTHENTICATION_REQUIRED);
        }
    }
}
