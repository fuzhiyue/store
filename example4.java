import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
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
 * Access Token Utility Class
 * Provides methods to extract token string from AccessToken object
 * and validate users using token string
 */
public class AccessTokenUtils {
    
    private static final Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create();
    
    /**
     * Extracts token string from AccessToken object
     * 
     * @param accessToken The NimbusDS AccessToken object
     * @return Token value as string
     * @throws IllegalArgumentException if accessToken is null
     */
    public static String extractTokenString(AccessToken accessToken) {
        if (accessToken == null) {
            throw new IllegalArgumentException("AccessToken cannot be null");
        }
        
        String tokenValue = accessToken.getValue();
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            throw new IllegalArgumentException("AccessToken value is null or empty");
        }
        
        return tokenValue;
    }
    
    /**
     * Creates a Bearer AccessToken from a token string
     * 
     * @param tokenString The token value as string
     * @return BearerAccessToken object
     */
    public static BearerAccessToken createBearerToken(String tokenString) {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            throw new IllegalArgumentException("Token string cannot be null or empty");
        }
        
        return new BearerAccessToken(tokenString);
    }
    
    /**
     * Validates a token string and extracts user information
     * 
     * @param tokenString The token string to validate
     * @param validatorConfig Configuration for the validator
     * @return UserValidationResult with user information
     */
    public static UserValidationResult validateTokenAndGetUser(String tokenString, 
                                                              ValidatorConfig validatorConfig) {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            return UserValidationResult.error("Token string is null or empty");
        }
        
        // Create validator instance
        TokenValidator validator = new TokenValidator(
            validatorConfig.getJwksUrl(),
            validatorConfig.getIntrospectionUrl(),
            validatorConfig.getClientId(),
            validatorConfig.getClientSecret(),
            validatorConfig.getExpectedIssuer(),
            validatorConfig.getExpectedAudience()
        );
        
        // Validate token
        TokenValidator.ValidationResult validationResult = validator.validate(createBearerToken(tokenString));
        
        if (!validationResult.isValid()) {
            return UserValidationResult.error(validationResult.getError());
        }
        
        // Create user validation result
        return UserValidationResult.success(
            validationResult.getSubject(),
            validationResult.getIssuer(),
            validationResult.getAudience(),
            validationResult.getScopes(),
            validationResult.getClientId(),
            validationResult.getExpirationTime(),
            validationResult.getIssueTime()
        );
    }
    
    /**
     * Validates if a user has required scopes
     * 
     * @param tokenString The token string
     * @param requiredScopes List of required scopes
     * @param validatorConfig Validator configuration
     * @return boolean indicating if user has all required scopes
     */
    public static boolean hasRequiredScopes(String tokenString, 
                                           List<String> requiredScopes,
                                           ValidatorConfig validatorConfig) {
        UserValidationResult result = validateTokenAndGetUser(tokenString, validatorConfig);
        
        if (!result.isValid()) {
            return false;
        }
        
        List<String> tokenScopes = result.getScopes();
        if (tokenScopes == null || tokenScopes.isEmpty()) {
            return requiredScopes == null || requiredScopes.isEmpty();
        }
        
        if (requiredScopes == null || requiredScopes.isEmpty()) {
            return true;
        }
        
        return tokenScopes.containsAll(requiredScopes);
    }
    
    /**
     * Validates if a user has at least one of the required scopes
     * 
     * @param tokenString The token string
     * @param anyOfScopes List of scopes, user must have at least one
     * @param validatorConfig Validator configuration
     * @return boolean indicating if user has at least one required scope
     */
    public static boolean hasAnyScope(String tokenString, 
                                     List<String> anyOfScopes,
                                     ValidatorConfig validatorConfig) {
        UserValidationResult result = validateTokenAndGetUser(tokenString, validatorConfig);
        
        if (!result.isValid()) {
            return false;
        }
        
        List<String> tokenScopes = result.getScopes();
        if (tokenScopes == null || tokenScopes.isEmpty()) {
            return false;
        }
        
        if (anyOfScopes == null || anyOfScopes.isEmpty()) {
            return true;
        }
        
        for (String scope : anyOfScopes) {
            if (tokenScopes.contains(scope)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Extracts user ID from token string
     * 
     * @param tokenString The token string
     * @param validatorConfig Validator configuration
     * @return User ID or null if validation fails
     */
    public static String extractUserId(String tokenString, ValidatorConfig validatorConfig) {
        UserValidationResult result = validateTokenAndGetUser(tokenString, validatorConfig);
        
        if (!result.isValid()) {
            return null;
        }
        
        return result.getUserId();
    }
    
    /**
     * Checks if token is a JWT token
     * 
     * @param tokenString The token string to check
     * @return true if token is JWT format, false otherwise
     */
    public static boolean isJWTToken(String tokenString) {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = tokenString.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        
        try {
            String headerJson = new String(java.util.Base64.getUrlDecoder()
                .decode(parts[0] + getBase64Padding(parts[0])));
            JsonObject header = JsonParser.parseString(headerJson).getAsJsonObject();
            return header.has("alg") && header.has("typ");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extracts claims from JWT token without validation
     * Use with caution - no signature verification
     * 
     * @param tokenString The JWT token string
     * @return Map of claims
     * @throws ParseException if token parsing fails
     */
    public static Map<String, Object> extractClaimsUnsafe(String tokenString) throws ParseException {
        if (!isJWTToken(tokenString)) {
            throw new ParseException("Token is not a JWT");
        }
        
        try {
            SignedJWT jwt = SignedJWT.parse(tokenString);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            
            Map<String, Object> claimMap = new HashMap<>();
            for (String claimName : claims.getClaims().keySet()) {
                claimMap.put(claimName, claims.getClaim(claimName));
            }
            
            return claimMap;
        } catch (java.text.ParseException e) {
            throw new ParseException("Failed to parse JWT: " + e.getMessage());
        }
    }
    
    /**
     * Token Validator class
     */
    public static class TokenValidator {
        private final String jwksUrl;
        private final String introspectionUrl;
        private final String clientId;
        private final String clientSecret;
        private final String expectedIssuer;
        private final List<String> expectedAudience;
        private final OkHttpClient httpClient;
        
        public TokenValidator(String jwksUrl, String introspectionUrl,
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
            
            if (isJWTToken(tokenValue)) {
                return validateJWTToken(tokenValue);
            } else {
                return validateOpaqueToken(tokenValue);
            }
        }
        
        private ValidationResult validateJWTToken(String tokenValue) {
            ValidationResult result = new ValidationResult();
            
            try {
                SignedJWT jwt = SignedJWT.parse(tokenValue);
                
                if (jwksUrl != null && !jwksUrl.isEmpty()) {
                    if (!validateJWTSignature(jwt)) {
                        result.setError("JWT signature validation failed");
                        return result;
                    }
                }
                
                JWTClaimsSet claims = jwt.getJWTClaimsSet();
                
                Date now = new Date();
                if (claims.getExpirationTime() != null && 
                    claims.getExpirationTime().before(now)) {
                    result.setError("Token expired");
                    return result;
                }
                
                if (claims.getNotBeforeTime() != null && 
                    claims.getNotBeforeTime().after(now)) {
                    result.setError("Token not yet valid");
                    return result;
                }
                
                String issuer = claims.getIssuer();
                if (expectedIssuer != null && !expectedIssuer.isEmpty()) {
                    if (issuer == null || !issuer.equals(expectedIssuer)) {
                        result.setError("Invalid issuer: " + issuer);
                        return result;
                    }
                }
                result.setIssuer(issuer);
                
                List<String> audience = claims.getAudience();
                if (expectedAudience != null && !expectedAudience.isEmpty()) {
                    if (audience == null || audience.isEmpty()) {
                        result.setError("Missing audience");
                        return result;
                    }
                    
                    boolean validAudience = false;
                    for (String expectedAud : expectedAudience) {
                        if (audience.contains(expectedAud)) {
                            validAudience = true;
                            break;
                        }
                    }
                    
                    if (!validAudience) {
                        result.setError("Invalid audience");
                        return result;
                    }
                }
                result.setAudience(audience);
                
                result.setSubject(claims.getSubject());
                result.setExpirationTime(claims.getExpirationTime());
                result.setIssueTime(claims.getIssueTime());
                
                Object scopeObj = claims.getClaim("scope");
                if (scopeObj instanceof String) {
                    String scopeStr = (String) scopeObj;
                    if (!scopeStr.trim().isEmpty()) {
                        result.setScopes(List.of(scopeStr.split(" ")));
                    }
                }
                
                Object clientIdObj = claims.getClaim("client_id");
                if (clientIdObj instanceof String) {
                    result.setClientId((String) clientIdObj);
                }
                
                result.setValid(true);
                return result;
                
            } catch (Exception e) {
                result.setError("JWT validation failed: " + e.getMessage());
                return result;
            }
        }
        
        private ValidationResult validateOpaqueToken(String tokenValue) {
            ValidationResult result = new ValidationResult();
            
            if (introspectionUrl == null || introspectionUrl.isEmpty()) {
                result.setError("Introspection endpoint not configured");
                return result;
            }
            
            try {
                Map<String, String> formData = new HashMap<>();
                formData.put("token", tokenValue);
                formData.put("token_type_hint", "access_token");
                formData.put("client_id", clientId);
                formData.put("client_secret", clientSecret);
                
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
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        result.setError("Introspection failed: " + response.code());
                        return result;
                    }
                    
                    String responseBody = response.body().string();
                    Type responseType = new TypeToken<Map<String, Object>>(){}.getType();
                    Map<String, Object> introspectionResult = gson.fromJson(responseBody, responseType);
                    
                    Boolean active = (Boolean) introspectionResult.get("active");
                    if (active == null || !active) {
                        result.setError("Token is not active");
                        return result;
                    }
                    
                    result.setSubject((String) introspectionResult.get("sub"));
                    result.setClientId((String) introspectionResult.get("client_id"));
                    result.setIssuer((String) introspectionResult.get("iss"));
                    
                    Object scopeObj = introspectionResult.get("scope");
                    if (scopeObj instanceof String) {
                        String scopeStr = (String) scopeObj;
                        if (!scopeStr.trim().isEmpty()) {
                            result.setScopes(List.of(scopeStr.split(" ")));
                        }
                    }
                    
                    Object audObj = introspectionResult.get("aud");
                    if (audObj instanceof String) {
                        result.setAudience(List.of((String) audObj));
                    } else if (audObj instanceof List) {
                        result.setAudience((List<String>) audObj);
                    }
                    
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
                return false;
            }
        }
        
        /**
         * Validation Result
         */
        public static class ValidationResult {
            private boolean valid = false;
            private String error;
            private String subject;
            private String issuer;
            private List<String> audience;
            private List<String> scopes;
            private String clientId;
            private Date expirationTime;
            private Date issueTime;
            
            public boolean isValid() { return valid; }
            public String getError() { return error; }
            public String getSubject() { return subject; }
            public String getIssuer() { return issuer; }
            public List<String> getAudience() { return audience; }
            public List<String> getScopes() { return scopes; }
            public String getClientId() { return clientId; }
            public Date getExpirationTime() { return expirationTime; }
            public Date getIssueTime() { return issueTime; }
            
            public void setValid(boolean valid) { this.valid = valid; }
            public void setError(String error) { this.error = error; }
            public void setSubject(String subject) { this.subject = subject; }
            public void setIssuer(String issuer) { this.issuer = issuer; }
            public void setAudience(List<String> audience) { this.audience = audience; }
            public void setScopes(List<String> scopes) { this.scopes = scopes; }
            public void setClientId(String clientId) { this.clientId = clientId; }
            public void setExpirationTime(Date expirationTime) { this.expirationTime = expirationTime; }
            public void setIssueTime(Date issueTime) { this.issueTime = issueTime; }
        }
    }
    
    /**
     * Validator Configuration
     */
    public static class ValidatorConfig {
        private String jwksUrl;
        private String introspectionUrl;
        private String clientId;
        private String clientSecret;
        private String expectedIssuer;
        private List<String> expectedAudience;
        
        public ValidatorConfig() {}
        
        public String getJwksUrl() { return jwksUrl; }
        public String getIntrospectionUrl() { return introspectionUrl; }
        public String getClientId() { return clientId; }
        public String getClientSecret() { return clientSecret; }
        public String getExpectedIssuer() { return expectedIssuer; }
        public List<String> getExpectedAudience() { return expectedAudience; }
        
        public ValidatorConfig setJwksUrl(String jwksUrl) { 
            this.jwksUrl = jwksUrl; 
            return this; 
        }
        
        public ValidatorConfig setIntrospectionUrl(String introspectionUrl) { 
            this.introspectionUrl = introspectionUrl; 
            return this; 
        }
        
        public ValidatorConfig setClientId(String clientId) { 
            this.clientId = clientId; 
            return this; 
        }
        
        public ValidatorConfig setClientSecret(String clientSecret) { 
            this.clientSecret = clientSecret; 
            return this; 
        }
        
        public ValidatorConfig setExpectedIssuer(String expectedIssuer) { 
            this.expectedIssuer = expectedIssuer; 
            return this; 
        }
        
        public ValidatorConfig setExpectedAudience(List<String> expectedAudience) { 
            this.expectedAudience = expectedAudience; 
            return this; 
        }
    }
    
    /**
     * User Validation Result
     */
    public static class UserValidationResult {
        private boolean valid;
        private String error;
        private String userId;
        private String issuer;
        private List<String> audience;
        private List<String> scopes;
        private String clientId;
        private Date expirationTime;
        private Date issueTime;
        
        private UserValidationResult(boolean valid, String error) {
            this.valid = valid;
            this.error = error;
        }
        
        private UserValidationResult(boolean valid, String userId, String issuer,
                                   List<String> audience, List<String> scopes,
                                   String clientId, Date expirationTime, Date issueTime) {
            this.valid = valid;
            this.userId = userId;
            this.issuer = issuer;
            this.audience = audience;
            this.scopes = scopes;
            this.clientId = clientId;
            this.expirationTime = expirationTime;
            this.issueTime = issueTime;
        }
        
        public static UserValidationResult error(String error) {
            return new UserValidationResult(false, error);
        }
        
        public static UserValidationResult success(String userId, String issuer,
                                                 List<String> audience, List<String> scopes,
                                                 String clientId, Date expirationTime, Date issueTime) {
            return new UserValidationResult(true, userId, issuer, audience, scopes, 
                                          clientId, expirationTime, issueTime);
        }
        
        public boolean isValid() { return valid; }
        public String getError() { return error; }
        public String getUserId() { return userId; }
        public String getIssuer() { return issuer; }
        public List<String> getAudience() { return audience; }
        public List<String> getScopes() { return scopes; }
        public String getClientId() { return clientId; }
        public Date getExpirationTime() { return expirationTime; }
        public Date getIssueTime() { return issueTime; }
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
     * Test method
     */
    public static void main(String[] args) {
        System.out.println("=== Access Token Utility Test ===\n");
        
        try {
            // Test 1: Extract token string from AccessToken
            System.out.println("Test 1: Extract token string from AccessToken");
            BearerAccessToken accessToken = new BearerAccessToken(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNTE2MjM5MDIyfQ.test",
                3600
            );
            
            String tokenString = extractTokenString(accessToken);
            System.out.println("Extracted token: " + tokenString.substring(0, 30) + "...");
            
            // Test 2: Create BearerToken from string
            System.out.println("\nTest 2: Create BearerToken from string");
            BearerAccessToken recreatedToken = createBearerToken(tokenString);
            System.out.println("Recreated token value: " + recreatedToken.getValue().substring(0, 30) + "...");
            
            // Test 3: Check if token is JWT
            System.out.println("\nTest 3: Check JWT token format");
            boolean isJWT = isJWTToken(tokenString);
            System.out.println("Is JWT token: " + isJWT);
            
            // Test 4: Extract unsafe claims
            System.out.println("\nTest 4: Extract claims (unsafe)");
            try {
                Map<String, Object> claims = extractClaimsUnsafe(tokenString);
                System.out.println("Claims extracted: " + claims.size());
                for (Map.Entry<String, Object> entry : claims.entrySet()) {
                    System.out.println("  " + entry.getKey() + ": " + entry.getValue());
                }
            } catch (ParseException e) {
                System.out.println("Failed to extract claims: " + e.getMessage());
            }
            
            // Test 5: Create validator config
            System.out.println("\nTest 5: Create validator configuration");
            ValidatorConfig config = new ValidatorConfig()
                .setJwksUrl("https://auth.example.com/.well-known/jwks.json")
                .setIntrospectionUrl("https://auth.example.com/introspect")
                .setClientId("api-client")
                .setClientSecret("client-secret")
                .setExpectedIssuer("https://auth.example.com")
                .setExpectedAudience(List.of("api-service"));
            
            System.out.println("Config created with issuer: " + config.getExpectedIssuer());
            
            // Test 6: Extract user ID
            System.out.println("\nTest 6: Extract user ID");
            String userId = extractUserId(tokenString, config);
            System.out.println("User ID extracted: " + userId);
            
            // Test 7: Test with opaque token
            System.out.println("\nTest 7: Test with opaque token");
            String opaqueToken = "opaque_token_123456";
            boolean isOpaqueJWT = isJWTToken(opaqueToken);
            System.out.println("Is opaque token JWT: " + isOpaqueJWT);
            
            System.out.println("\nAll tests completed!");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
