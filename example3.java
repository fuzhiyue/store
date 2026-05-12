import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * AccessToken JSON Converter and Validator
 * Converts NimbusDS AccessToken objects to/from JSON for RPC communication
 * Provides token validation methods using NimbusDS
 */
public class AccessTokenJsonConverter {
    
    private static final Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create();
    
    /**
     * Converts AccessToken to JSON string for RPC transmission
     * 
     * @param accessToken The NimbusDS AccessToken object
     * @return JSON string representation
     * @throws IllegalArgumentException if accessToken is null
     */
    public static String toJson(AccessToken accessToken) {
        if (accessToken == null) {
            throw new IllegalArgumentException("AccessToken cannot be null");
        }
        
        Map<String, Object> tokenData = new HashMap<>();
        
        // Basic token properties
        tokenData.put("value", accessToken.getValue());
        tokenData.put("token_type", accessToken instanceof BearerAccessToken ? "Bearer" : "AccessToken");
        tokenData.put("lifetime", accessToken.getLifetime());
        
        // Scope information
        Scope scope = accessToken.getScope();
        if (scope != null) {
            tokenData.put("scope", scope.toStringList());
        }
        
        // Extract all parameters
        Map<String, String> parameters = new HashMap<>();
        for (String paramName : accessToken.getParameterNames()) {
            String paramValue = accessToken.getParameter(paramName);
            if (paramValue != null) {
                parameters.put(paramName, paramValue);
            }
        }
        if (!parameters.isEmpty()) {
            tokenData.put("parameters", parameters);
        }
        
        // Add metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("converted_at", new Date().toString());
        metadata.put("converter_version", "1.0");
        tokenData.put("metadata", metadata);
        
        return gson.toJson(tokenData);
    }
    
    /**
     * Converts JSON string back to AccessToken object
     * 
     * @param jsonString JSON string from RPC
     * @return NimbusDS AccessToken object
     * @throws ParseException if JSON parsing or token reconstruction fails
     */
    public static AccessToken fromJson(String jsonString) throws ParseException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            throw new ParseException("JSON string cannot be null or empty");
        }
        
        try {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> tokenData = gson.fromJson(jsonString, type);
            
            // Extract token value
            String tokenValue = (String) tokenData.get("value");
            if (tokenValue == null || tokenValue.trim().isEmpty()) {
                throw new ParseException("Missing or empty 'value' field in JSON");
            }
            
            // Extract scope
            Scope scope = null;
            if (tokenData.containsKey("scope")) {
                Object scopeObj = tokenData.get("scope");
                if (scopeObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> scopeList = (List<String>) scopeObj;
                    if (!scopeList.isEmpty()) {
                        scope = new Scope(scopeList.toArray(new String[0]));
                    }
                } else if (scopeObj instanceof String) {
                    String scopeStr = (String) scopeObj;
                    if (!scopeStr.trim().isEmpty()) {
                        scope = new Scope(scopeStr);
                    }
                }
            }
            
            // Extract lifetime
            long lifetime = 0;
            if (tokenData.containsKey("lifetime")) {
                Object lifetimeObj = tokenData.get("lifetime");
                if (lifetimeObj instanceof Number) {
                    lifetime = ((Number) lifetimeObj).longValue();
                } else if (lifetimeObj instanceof String) {
                    try {
                        lifetime = Long.parseLong((String) lifetimeObj);
                    } catch (NumberFormatException e) {
                        lifetime = 0;
                    }
                }
            }
            
            // Create BearerAccessToken
            BearerAccessToken token = new BearerAccessToken(tokenValue, lifetime, scope);
            
            // Restore parameters
            if (tokenData.containsKey("parameters")) {
                @SuppressWarnings("unchecked")
                Map<String, String> parameters = (Map<String, String>) tokenData.get("parameters");
                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    if (entry.getValue() != null) {
                        token.setParameter(entry.getKey(), entry.getValue());
                    }
                }
            }
            
            return token;
            
        } catch (Exception e) {
            throw new ParseException("Failed to convert JSON to AccessToken: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a string is likely a JWT token
     */
    public static boolean isJWTToken(String tokenValue) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = tokenValue.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        
        // Check if it starts with typical JWT header
        try {
            String headerJson = new String(java.util.Base64.getUrlDecoder()
                .decode(parts[0] + getBase64Padding(parts[0])));
            JsonObject header = JsonParser.parseString(headerJson).getAsJsonObject();
            return header.has("alg") && header.has("typ");
        } catch (Exception e) {
            return false;
        }
    }
    
    private static String getBase64Padding(String base64String) {
        int remainder = base64String.length() % 4;
        switch (remainder) {
            case 0:
                return "";
            case 1:
                throw new IllegalArgumentException("Invalid base64 string");
            case 2:
                return "==";
            case 3:
                return "=";
            default:
                return "";
        }
    }
    
    /**
     * Token Validation Result
     */
    public static class ValidationResult {
        private boolean valid;
        private String error;
        private String subject;
        private String issuer;
        private List<String> audience;
        private List<String> scopes;
        private String clientId;
        private Date expirationTime;
        private Date issueTime;
        private String tokenType;
        private Map<String, Object> customClaims;
        
        public ValidationResult() {
            this.valid = false;
            this.customClaims = new HashMap<>();
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getError() { return error; }
        public String getSubject() { return subject; }
        public String getIssuer() { return issuer; }
        public List<String> getAudience() { return audience; }
        public List<String> getScopes() { return scopes; }
        public String getClientId() { return clientId; }
        public Date getExpirationTime() { return expirationTime; }
        public Date getIssueTime() { return issueTime; }
        public String getTokenType() { return tokenType; }
        public Map<String, Object> getCustomClaims() { return customClaims; }
        
        // Setters
        public void setValid(boolean valid) { this.valid = valid; }
        public void setError(String error) { this.error = error; }
        public void setSubject(String subject) { this.subject = subject; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public void setAudience(List<String> audience) { this.audience = audience; }
        public void setScopes(List<String> scopes) { this.scopes = scopes; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public void setExpirationTime(Date expirationTime) { this.expirationTime = expirationTime; }
        public void setIssueTime(Date issueTime) { this.issueTime = issueTime; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public void addCustomClaim(String name, Object value) { this.customClaims.put(name, value); }
        
        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, subject='%s', error='%s'}", 
                valid, subject, error);
        }
    }
    
    /**
     * Access Token Validator using NimbusDS
     */
    public static class AccessTokenValidator {
        private final String jwksUrl;
        private final String introspectionUrl;
        private final String clientId;
        private final String clientSecret;
        private final String expectedIssuer;
        private final List<String> expectedAudience;
        private final OkHttpClient httpClient;
        
        public AccessTokenValidator(String jwksUrl, String introspectionUrl,
                                   String clientId, String clientSecret,
                                   String expectedIssuer, List<String> expectedAudience) {
            this.jwksUrl = jwksUrl;
            this.introspectionUrl = introspectionUrl;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.expectedIssuer = expectedIssuer;
            this.expectedAudience = expectedAudience;
            
            this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        }
        
        /**
         * Validates an AccessToken
         * 
         * @param accessToken The AccessToken to validate
         * @return ValidationResult with validation outcome
         */
        public ValidationResult validate(AccessToken accessToken) {
            ValidationResult result = new ValidationResult();
            
            if (accessToken == null) {
                result.setError("AccessToken is null");
                return result;
            }
            
            String tokenValue = accessToken.getValue();
            if (tokenValue == null || tokenValue.trim().isEmpty()) {
                result.setError("AccessToken value is empty");
                return result;
            }
            
            // Determine validation strategy based on token type
            if (isJWTToken(tokenValue)) {
                return validateJWTToken(tokenValue, accessToken);
            } else {
                return validateOpaqueToken(tokenValue, accessToken);
            }
        }
        
        /**
         * Validates a JWT Access Token
         */
        private ValidationResult validateJWTToken(String tokenValue, AccessToken accessToken) {
            ValidationResult result = new ValidationResult();
            result.setTokenType("JWT");
            
            try {
                // Parse JWT
                SignedJWT signedJWT = SignedJWT.parse(tokenValue);
                
                // Validate signature if JWKS URL is configured
                if (jwksUrl != null && !jwksUrl.isEmpty()) {
                    if (!validateJWTSignature(signedJWT)) {
                        result.setError("JWT signature validation failed");
                        return result;
                    }
                }
                
                // Get claims
                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
                
                // Validate expiration
                Date currentTime = new Date();
                Date expirationTime = claims.getExpirationTime();
                Date notBeforeTime = claims.getNotBeforeTime();
                
                if (expirationTime != null && expirationTime.before(currentTime)) {
                    result.setError("Token expired at " + expirationTime);
                    return result;
                }
                
                if (notBeforeTime != null && notBeforeTime.after(currentTime)) {
                    result.setError("Token not valid until " + notBeforeTime);
                    return result;
                }
                
                // Validate issuer
                String issuer = claims.getIssuer();
                if (expectedIssuer != null && !expectedIssuer.isEmpty()) {
                    if (issuer == null || !issuer.equals(expectedIssuer)) {
                        result.setError("Invalid issuer: " + issuer + ", expected: " + expectedIssuer);
                        return result;
                    }
                }
                result.setIssuer(issuer);
                
                // Validate audience
                List<String> audience = claims.getAudience();
                if (expectedAudience != null && !expectedAudience.isEmpty()) {
                    if (audience == null || audience.isEmpty()) {
                        result.setError("Token has no audience, but audience validation is required");
                        return result;
                    }
                    
                    boolean audienceValid = false;
                    for (String expectedAud : expectedAudience) {
                        if (audience.contains(expectedAud)) {
                            audienceValid = true;
                            break;
                        }
                    }
                    
                    if (!audienceValid) {
                        result.setError("Invalid audience: " + audience + ", expected one of: " + expectedAudience);
                        return result;
                    }
                }
                result.setAudience(audience);
                
                // Extract user information
                result.setSubject(claims.getSubject());
                result.setExpirationTime(expirationTime);
                result.setIssueTime(claims.getIssueTime());
                
                // Extract scopes
                Object scopeObj = claims.getClaim("scope");
                if (scopeObj instanceof String) {
                    String scopeStr = (String) scopeObj;
                    if (!scopeStr.trim().isEmpty()) {
                        result.setScopes(List.of(scopeStr.split(" ")));
                    }
                }
                
                // Extract client_id
                Object clientIdObj = claims.getClaim("client_id");
                if (clientIdObj instanceof String) {
                    result.setClientId((String) clientIdObj);
                }
                
                // Extract custom claims
                for (String claimName : claims.getClaims().keySet()) {
                    if (!isStandardClaim(claimName)) {
                        result.addCustomClaim(claimName, claims.getClaim(claimName));
                    }
                }
                
                result.setValid(true);
                return result;
                
            } catch (Exception e) {
                result.setError("JWT validation failed: " + e.getMessage());
                return result;
            }
        }
        
        /**
         * Validates an opaque Access Token using introspection
         */
        private ValidationResult validateOpaqueToken(String tokenValue, AccessToken accessToken) {
            ValidationResult result = new ValidationResult();
            result.setTokenType("OPAQUE");
            
            if (introspectionUrl == null || introspectionUrl.isEmpty()) {
                result.setError("Introspection endpoint not configured for opaque token");
                return result;
            }
            
            try {
                // Prepare introspection request
                Map<String, String> formData = new HashMap<>();
                formData.put("token", tokenValue);
                formData.put("token_type_hint", "access_token");
                formData.put("client_id", clientId);
                formData.put("client_secret", clientSecret);
                
                // Build form body
                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                StringBuilder formBody = new StringBuilder();
                for (Map.Entry<String, String> entry : formData.entrySet()) {
                    if (formBody.length() > 0) {
                        formBody.append("&");
                    }
                    formBody.append(entry.getKey())
                           .append("=")
                           .append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                
                RequestBody body = RequestBody.create(formBody.toString(), mediaType);
                
                Request request = new Request.Builder()
                    .url(introspectionUrl)
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .build();
                
                // Execute request
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        result.setError(String.format("Introspection request failed: %d %s", 
                            response.code(), response.message()));
                        return result;
                    }
                    
                    String responseBody = response.body().string();
                    Type responseType = new TypeToken<Map<String, Object>>(){}.getType();
                    Map<String, Object> introspectionResult = gson.fromJson(responseBody, responseType);
                    
                    // Check if token is active
                    Boolean active = (Boolean) introspectionResult.get("active");
                    if (active == null || !active) {
                        result.setError("Token is not active or has been revoked");
                        return result;
                    }
                    
                    // Extract information
                    result.setSubject((String) introspectionResult.get("sub"));
                    result.setClientId((String) introspectionResult.get("client_id"));
                    result.setIssuer((String) introspectionResult.get("iss"));
                    
                    // Extract scopes
                    Object scopeObj = introspectionResult.get("scope");
                    if (scopeObj instanceof String) {
                        String scopeStr = (String) scopeObj;
                        if (!scopeStr.trim().isEmpty()) {
                            result.setScopes(List.of(scopeStr.split(" ")));
                        }
                    }
                    
                    // Extract audience
                    Object audObj = introspectionResult.get("aud");
                    if (audObj instanceof String) {
                        result.setAudience(List.of((String) audObj));
                    } else if (audObj instanceof List) {
                        result.setAudience((List<String>) audObj);
                    }
                    
                    // Extract expiration time
                    Object expObj = introspectionResult.get("exp");
                    if (expObj instanceof Number) {
                        long exp = ((Number) expObj).longValue();
                        result.setExpirationTime(new Date(exp * 1000));
                    }
                    
                    result.setValid(true);
                    return result;
                }
                
            } catch (Exception e) {
                result.setError("Introspection failed: " + e.getMessage());
                return result;
            }
        }
        
        /**
         * Validates JWT signature using JWKS endpoint
         */
        private boolean validateJWTSignature(SignedJWT jwt) {
            try {
                ResourceRetriever resourceRetriever = new DefaultResourceRetriever(10000, 10000);
                
                JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(
                    new URL(jwksUrl), 
                    resourceRetriever
                );
                
                DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
                
                JWSVerificationKeySelector<SecurityContext> keySelector = 
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);
                
                jwtProcessor.setJWSKeySelector(keySelector);
                
                jwtProcessor.process(jwt, null);
                
                return true;
            } catch (Exception e) {
                System.err.println("JWT signature validation failed: " + e.getMessage());
                return false;
            }
        }
        
        private boolean isStandardClaim(String claimName) {
            Set<String> standardClaims = new HashSet<>();
            standardClaims.add("iss");
            standardClaims.add("sub");
            standardClaims.add("aud");
            standardClaims.add("exp");
            standardClaims.add("nbf");
            standardClaims.add("iat");
            standardClaims.add("jti");
            standardClaims.add("scope");
            standardClaims.add("client_id");
            
            return standardClaims.contains(claimName);
        }
    }
    
    /**
     * Test method demonstrating usage
     */
    public static void main(String[] args) {
        System.out.println("=== AccessToken JSON Converter and Validator Test ===\n");
        
        try {
            // Create a sample AccessToken
            Scope scope = new Scope("read", "write", "profile");
            BearerAccessToken accessToken = new BearerAccessToken(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwibmFtZSI6IkpvaG4gRG9lIiwiYXVkIjpbImFwaS1zZXJ2aWNlIl0sImlzcyI6Imh0dHBzOi8vYXV0aC5leGFtcGxlLmNvbSIsInNjb3BlIjoicmVhZCB3cml0ZSIsImV4cCI6MTgwMDAwMDAwMH0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                3600,
                scope
            );
            accessToken.setParameter("client_id", "api-client-123");
            accessToken.setParameter("issued_at", "2024-01-01T00:00:00Z");
            
            // Test 1: Convert to JSON
            System.out.println("Test 1: Convert AccessToken to JSON");
            String json = toJson(accessToken);
            System.out.println("JSON output:");
            System.out.println(json);
            System.out.println();
            
            // Test 2: Convert back from JSON
            System.out.println("Test 2: Convert JSON back to AccessToken");
            AccessToken restoredToken = fromJson(json);
            System.out.println("Restored token value: " + restoredToken.getValue());
            System.out.println("Restored token scope: " + restoredToken.getScope());
            System.out.println("Restored token lifetime: " + restoredToken.getLifetime());
            System.out.println("Restored client_id parameter: " + restoredToken.getParameter("client_id"));
            System.out.println();
            
            // Test 3: JWT detection
            System.out.println("Test 3: JWT Token Detection");
            String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTUxNjIzOTAyMn0.test";
            String opaqueToken = "abc123def456ghi789";
            
            System.out.println("Is JWT Token? " + isJWTToken(jwtToken));
            System.out.println("Is Opaque Token? " + !isJWTToken(opaqueToken));
            System.out.println();
            
            // Test 4: Validation (simulated)
            System.out.println("Test 4: Token Validation Setup");
            AccessTokenValidator validator = new AccessTokenValidator(
                "https://auth.example.com/.well-known/jwks.json",
                "https://auth.example.com/introspect",
                "api-client",
                "client-secret",
                "https://auth.example.com",
                List.of("api-service", "backend-service")
            );
            
            // Note: Actual validation would require a real JWKS endpoint
            System.out.println("Validator created with configuration:");
            System.out.println("  - JWKS URL: " + validator.jwksUrl);
            System.out.println("  - Introspection URL: " + validator.introspectionUrl);
            System.out.println("  - Expected Issuer: " + validator.expectedIssuer);
            System.out.println("  - Expected Audience: " + validator.expectedAudience);
            System.out.println();
            
            // Test 5: Complete RPC flow simulation
            System.out.println("Test 5: Simulating RPC Flow");
            
            // Client side: Convert token to JSON
            String tokenJson = toJson(accessToken);
            
            // Server side: Convert back and validate
            AccessToken receivedToken = fromJson(tokenJson);
            System.out.println("Received token in RPC: " + receivedToken.getValue().substring(0, 30) + "...");
            System.out.println("RPC flow simulation completed successfully");
            
        } catch (ParseException e) {
            System.err.println("Parse Exception: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
