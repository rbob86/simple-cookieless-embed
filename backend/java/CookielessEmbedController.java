package backend.java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;



@RestController
public class CookielessEmbedController {

    // Environment-based configuration (or use application.properties, etc.)
    private static final String LOOKER_BASE_URL = System.getenv("LOOKER_BASE_URL"); // e.g.
                                                                                    // "https://mylooker.company.com:19999"
    private static final String LOOKER_CLIENT_ID = System.getenv("LOOKER_CLIENT_ID");
    private static final String LOOKER_CLIENT_SECRET = System.getenv("LOOKER_CLIENT_SECRET");

    private String externalUserId; // "embed-user-1"
    private String firstName; // "Johnny"
    private String lastName; // "Embed"
    private Integer sessionLength; // 3600
    private Boolean forceLogoutLogin; // true
    private String externalGroupId; // "embed-group-1"
    private List<Long> groupIds; // []
    private List<String> permissions; // [ "access_data", "see_user_dashboards", "explore", ... ]
    private List<String> models; // ["Runner", "thelook", "geo-test"]
    private Map<String, String> userAttributes; // e.

    // Minimal user info for embed session
    private static final EmbedUser EMBED_USER = new EmbedUser(
            "external-user-11",
            "Johnny",
            "Embed",
            3600,
            true,
            "embed-group-1",
            Collections.emptyList(),
            Arrays.asList("access_data", "see_user_dashboards", "see_lookml_dashboards", "explore", "see_looks"),
            Arrays.asList("models1"),
            Map.of("locale", "en_US"));

    // Session reference tokens stored in-memory for each user/session
    private final Map<String, String> sessionReferenceStore = new HashMap<>();
    private static final String SESSION_KEY = "johnny-embed";

    // We'll store one Looker API token in memory for simplicity
    private String lookerApiToken = null;

    // HTTP client and JSON utilities
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Acquire (or rejoin) a cookieless embed session.
     * Mirrors the Node.js `/acquire-embed-session`.
     */
    @GetMapping("/acquire-embed-session")
    public ResponseEntity<?> acquireEmbedSession(HttpServletRequest request) {
        try {
            // 1. Ensure we have a valid Looker API token
            String token = ensureLookerApiToken();

            // 2. Pull existing session_reference_token (if any)
            String sessionReferenceToken = sessionReferenceStore.getOrDefault(SESSION_KEY, "");

            // 3. Build request body
            Map<String, Object> body = new HashMap<>();
            body.put("external_user_id", EMBED_USER.getExternalUserId());
            body.put("external_group_id", EMBED_USER.getExternalGroupId());
            body.put("first_name", EMBED_USER.getFirstName());
            body.put("last_name", EMBED_USER.getLastName());
            body.put("email", EMBED_USER.getEmail());
            // ...
            // Add any permissions, user attributes, etc
            // ...
            body.put("session_reference_token", sessionReferenceToken);

            // 4. POST to /api/4.0/acquire_embed_cookieless_session
            String userAgent = request.getHeader("User-Agent");
            JsonNode responseJson = doPostJson(
                    LOOKER_BASE_URL + "/api/4.0/acquire_embed_cookieless_session",
                    body,
                    token,
                    userAgent);

            // 5. Parse the JSON response
            String newSRT = responseJson.path("session_reference_token").asText();
            if (newSRT != null && !newSRT.isEmpty()) {
                sessionReferenceStore.put(SESSION_KEY, newSRT);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("authentication_token", responseJson.path("authentication_token").asText());
            result.put("authentication_token_ttl", responseJson.path("authentication_token_ttl").asInt());
            result.put("navigation_token", responseJson.path("navigation_token").asText());
            result.put("navigation_token_ttl", responseJson.path("navigation_token_ttl").asInt());
            result.put("session_reference_token_ttl", responseJson.path("session_reference_token_ttl").asInt());
            result.put("api_token", responseJson.path("api_token").asText());
            result.put("api_token_ttl", responseJson.path("api_token_ttl").asInt());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Generate new embed tokens (periodically called by the Looker UI).
     * Mirrors the Node.js `/generate-embed-tokens`.
     */
    @PutMapping("/generate-embed-tokens")
    public ResponseEntity<?> generateEmbedTokens(HttpServletRequest request,
            @RequestBody GenerateTokensRequestBody body) {
        try {
            // 1. Check if we have a current session_reference_token
            String sessionReferenceToken = sessionReferenceStore.get(SESSION_KEY);
            if (sessionReferenceToken == null) {
                // No session => treat as expired
                return ResponseEntity.ok(Map.of("session_reference_token_ttl", 0));
            }

            // 2. Ensure we have a valid Looker API token
            String token = ensureLookerApiToken();

            // 3. Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("session_reference_token", sessionReferenceToken);
            requestBody.put("api_token", body.getApiToken());
            requestBody.put("navigation_token", body.getNavigationToken());

            // 4. POST to /api/4.0/generate_tokens_for_cookieless_session
            String userAgent = request.getHeader("User-Agent");
            JsonNode responseJson = doPostJson(
                    LOOKER_BASE_URL + "/api/4.0/generate_tokens_for_cookieless_session",
                    requestBody,
                    token,
                    userAgent);

            // 5. Build response for front-end
            Map<String, Object> result = new HashMap<>();
            result.put("api_token", responseJson.path("api_token").asText());
            result.put("api_token_ttl", responseJson.path("api_token_ttl").asInt());
            result.put("navigation_token", responseJson.path("navigation_token").asText());
            result.put("navigation_token_ttl", responseJson.path("navigation_token_ttl").asInt());
            result.put("session_reference_token_ttl", responseJson.path("session_reference_token_ttl").asInt());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            // Check for "Invalid input tokens provided"
            if (e.getMessage() != null && e.getMessage().contains("Invalid input tokens provided")) {
                return ResponseEntity.ok(Map.of("session_reference_token_ttl", 0));
            }
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // --------------------------------------------------------------------------------------
    // SINGLE FUNCTION: Ensure we have a valid Looker API token (logs in if we don't
    // have one)
    // --------------------------------------------------------------------------------------
    private synchronized String ensureLookerApiToken() throws IOException {
        // If we already have a token, just return it.
        // (For production, you'd also verify it's not expired, or catch 401 and
        // refresh.)
        if (lookerApiToken != null) {
            return lookerApiToken;
        }

        // Otherwise, login to Looker
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("client_id", LOOKER_CLIENT_ID);
        loginPayload.put("client_secret", LOOKER_CLIENT_SECRET);

        // POST /api/4.0/login => { "access_token": "...", "token_type": "bearer" }
        JsonNode json = doPostJson(
                LOOKER_BASE_URL + "/api/4.0/login",
                loginPayload,
                null, // no token needed to login
                null // no user-agent needed for login
        );

        // Extract the access_token
        String newToken = json.path("access_token").asText();
        if (newToken == null || newToken.isEmpty()) {
            throw new IOException("Failed to retrieve Looker API token");
        }

        lookerApiToken = newToken;
        return lookerApiToken;
    }

    // ----------------------------------------------------------------------
    // Helper to POST JSON to a Looker endpoint and parse the JSON response
    // ----------------------------------------------------------------------
    private JsonNode doPostJson(
            String url,
            Map<String, ?> body,
            String apiToken,
            String userAgent) throws IOException {
        // Convert request body to JSON
        String jsonBody = objectMapper.writeValueAsString(body);

        // Build the request
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")));

        // If we have an API token, use it
        if (apiToken != null && !apiToken.isEmpty()) {
            // Looker expects "Authorization: Bearer <access_token>"
            requestBuilder.addHeader("Authorization", "Bearer " + apiToken);
        }
        // If we have a user-agent, pass it (important for cookieless embedding)
        if (userAgent != null && !userAgent.isEmpty()) {
            requestBuilder.addHeader("User-Agent", userAgent);
        }

        // Execute
        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            String responseBody = (response.body() != null) ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + ": " + responseBody);
            }
            return objectMapper.readTree(responseBody);
        }
    }
}