import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.oauth2.sdk.ParseException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.text.ParseException as JWTParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Type;

/**
 * ID Token Converter Utility Class
 * Handles conversion between ID Token and JSON string
 */
public class IDTokenConverter {
    
    private static final Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create();
    
    /**
     * Check if a string is an ID Token JSON representation
     * 
     * @param jsonString The JSON string to check
     * @return true if it's an ID Token JSON representation, false otherwise
     */
    public static boolean isIDTokenJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Try to parse as JSON
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            if (!jsonElement.isJsonObject()) {
                return false;
            }
            
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            
            // Check if it has JWT string field
            if (jsonObject.has("jwt_string")) {
                String jwtString = jsonObject.get("jwt_string").getAsString();
                return isValidJWTString(jwtString);
            }
            
            // Check if it's simplified format
            if (jsonObject.has("jwt") && jsonObject.has("type")) {
                String type = jsonObject.get("type").getAsString();
                String jwtString = jsonObject.get("jwt").getAsString();
                
                if ("id_token".equals(type) && isValidJWTString(jwtString)) {
                    return true;
                }
            }
            
            // Check if it has complete ID Token structure
            boolean hasClaims = jsonObject.has("claims");
            boolean hasHeader = jsonObject.has("header");
            
            if (hasClaims && hasHeader) {
                // Check if claims have required ID Token fields
                JsonObject claims = jsonObject.getAsJsonObject("claims");
                if (claims.has("iss") && claims.has("sub") && claims.has("aud")) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            // Parsing failed, not valid JSON
            return false;
        }
    }
    
    /**
     * Check if it's a valid JWT string
     */
    private static boolean isValidJWTString(String jwtString) {
        if (jwtString == null || jwtString.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Check JWT basic format
            String[] parts = jwtString.split("\\.");
            
            // Plain JWT has 3 parts (header.payload.signature)
            // Signed JWT has 3 parts
            // Encrypted JWT has 5 parts
            if (parts.length != 3 && parts.length != 5) {
                return false;
            }
            
            // Try to parse header
            String headerJson = new String(java.util.Base64.getUrlDecoder()
                .decode(parts[0] + getPadding(parts[0])));
            JsonObject header = JsonParser.parseString(headerJson).getAsJsonObject();
            
            // Check if it has typ field
            if (!header.has("typ")) {
                return false;
            }
            
            String typ = header.get("typ").getAsString();
            return "JWT".equals(typ);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Add padding to base64 string if needed
     */
    private static String getPadding(String base64String) {
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
     * Check if it's simplified format ID Token JSON
     */
    public static boolean isSimpleIDTokenJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            
            boolean hasJwt = jsonObject.has("jwt");
            boolean hasType = jsonObject.has("type");
            
            if (hasJwt && hasType) {
                String type = jsonObject.get("type").getAsString();
                String jwt = jsonObject.get("jwt").getAsString();
                return "id_token".equals(type) && isValidJWTString(jwt);
            }
            
            return false;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if it's full format ID Token JSON
     */
    public static boolean isFullIDTokenJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            
            boolean hasJwtString = jsonObject.has("jwt_string");
            boolean hasHeader = jsonObject.has("header");
            boolean hasClaims = jsonObject.has("claims");
            
            if (!hasJwtString || !hasHeader || !hasClaims) {
                return false;
            }
            
            // Verify JWT string
            String jwtString = jsonObject.get("jwt_string").getAsString();
            if (!isValidJWTString(jwtString)) {
                return false;
            }
            
            // Check if claims have required ID Token fields
            JsonObject claims = jsonObject.getAsJsonObject("claims");
            return claims.has("iss") && claims.has("sub") && claims.has("aud");
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get the ID Token JSON format type
     */
    public static IDTokenJsonFormat getIDTokenJsonFormat(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return IDTokenJsonFormat.NOT_ID_TOKEN;
        }
        
        try {
            // First check if it's a pure JWT string
            if (isValidJWTString(jsonString)) {
                return IDTokenJsonFormat.PURE_JWT;
            }
            
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            
            // Check if it's simplified format
            if (jsonObject.has("jwt") && jsonObject.has("type")) {
                String type = jsonObject.get("type").getAsString();
                String jwt = jsonObject.get("jwt").getAsString();
                
                if ("id_token".equals(type) && isValidJWTString(jwt)) {
                    return IDTokenJsonFormat.SIMPLE;
                }
            }
            
            // Check if it's full format
            if (jsonObject.has("jwt_string") && 
                jsonObject.has("header") && 
                jsonObject.has("claims")) {
                
                String jwtString = jsonObject.get("jwt_string").getAsString();
                JsonObject claims = jsonObject.getAsJsonObject("claims");
                
                if (isValidJWTString(jwtString) && 
                    claims.has("iss") && 
                    claims.has("sub") && 
                    claims.has("aud")) {
                    return IDTokenJsonFormat.FULL;
                }
            }
            
            return IDTokenJsonFormat.NOT_ID_TOKEN;
            
        } catch (Exception e) {
            return IDTokenJsonFormat.NOT_ID_TOKEN;
        }
    }
    
    /**
     * ID Token JSON format enumeration
     */
    public enum IDTokenJsonFormat {
        NOT_ID_TOKEN,  // Not an ID Token
        SIMPLE,        // Simplified format: {"jwt": "...", "type": "id_token"}
        FULL,          // Full format: {"jwt_string": "...", "header": {...}, "claims": {...}}
        PURE_JWT       // Pure JWT string
    }
    
    /**
     * Parse any format JSON string to JWT
     */
    public static JWT parseAnyIDTokenJson(String jsonString) throws ParseException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        
        IDTokenJsonFormat format = getIDTokenJsonFormat(jsonString);
        
        switch (format) {
            case SIMPLE:
                return simpleJsonToIDToken(jsonString);
            case FULL:
                return jsonToIDToken(jsonString);
            case PURE_JWT:
                return parseJWTString(jsonString);
            case NOT_ID_TOKEN:
            default:
                throw new ParseException("Not a valid ID Token JSON format: " + format);
        }
    }
    
    /**
     * Convert ID Token to JSON string
     */
    public static String idTokenToJson(JWT idToken) throws ParseException {
        if (idToken == null) {
            return null;
        }
        
        try {
            Map<String, Object> tokenMap = new HashMap<>();
            
            // 1. Basic information
            tokenMap.put("jwt_string", idToken.serialize());
            tokenMap.put("jwt_type", getJWTType(idToken));
            
            // 2. Header information
            Map<String, Object> headerMap = new HashMap<>();
            if (idToken instanceof SignedJWT) {
                JWSHeader header = ((SignedJWT) idToken).getHeader();
                headerMap.put("algorithm", header.getAlgorithm().getName());
                headerMap.put("type", header.getType().getType());
                headerMap.put("key_id", header.getKeyID());
            } else if (idToken instanceof EncryptedJWT) {
                JWEHeader header = ((EncryptedJWT) idToken).getHeader();
                headerMap.put("algorithm", header.getAlgorithm().getName());
                headerMap.put("encryption_method", header.getEncryptionMethod().getName());
                headerMap.put("key_id", header.getKeyID());
            } else if (idToken instanceof PlainJWT) {
                headerMap.put("algorithm", "none");
            }
            tokenMap.put("header", headerMap);
            
            // 3. Claims information
            JWTClaimsSet claimsSet = idToken.getJWTClaimsSet();
            Map<String, Object> claimsMap = new HashMap<>();
            
            // Standard claims
            if (claimsSet.getIssuer() != null) {
                claimsMap.put("iss", claimsSet.getIssuer());
            }
            if (claimsSet.getSubject() != null) {
                claimsMap.put("sub", claimsSet.getSubject());
            }
            if (claimsSet.getAudience() != null && !claimsSet.getAudience().isEmpty()) {
                claimsMap.put("aud", claimsSet.getAudience());
            }
            if (claimsSet.getExpirationTime() != null) {
                claimsMap.put("exp", claimsSet.getExpirationTime());
            }
            if (claimsSet.getIssueTime() != null) {
                claimsMap.put("iat", claimsSet.getIssueTime());
            }
            if (claimsSet.getNotBeforeTime() != null) {
                claimsMap.put("nbf", claimsSet.getNotBeforeTime());
            }
            if (claimsSet.getJWTID() != null) {
                claimsMap.put("jti", claimsSet.getJWTID());
            }
            
            // Custom claims
            for (String claimName : claimsSet.getClaims().keySet()) {
                if (!isStandardClaim(claimName)) {
                    claimsMap.put(claimName, claimsSet.getClaim(claimName));
                }
            }
            
            tokenMap.put("claims", claimsMap);
            
            // 4. Signature information (only for SignedJWT)
            if (idToken instanceof SignedJWT) {
                SignedJWT signedJWT = (SignedJWT) idToken;
                String signature = signedJWT.getSignature().toString();
                tokenMap.put("signature", signature);
            }
            
            return gson.toJson(tokenMap);
            
        } catch (Exception e) {
            throw new ParseException("Failed to convert ID Token to JSON: " + e.getMessage());
        }
    }
    
    /**
     * Convert JSON string back to ID Token
     */
    public static JWT jsonToIDToken(String jsonString) throws ParseException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        
        try {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> tokenMap = gson.fromJson(jsonString, type);
            
            // Check if it contains JWT string
            String jwtString = (String) tokenMap.get("jwt_string");
            if (jwtString != null && !jwtString.trim().isEmpty()) {
                // Directly parse JWT string
                return parseJWTString(jwtString);
            }
            
            // If no JWT string, try to reconstruct from parts
            // Note: Not recommended in practice because signature cannot be reconstructed
            throw new ParseException("JWT string is required for reconstruction");
            
        } catch (Exception e) {
            throw new ParseException("Failed to convert JSON to ID Token: " + e.getMessage());
        }
    }
    
    /**
     * Parse JWT string
     */
    private static JWT parseJWTString(String jwtString) throws ParseException {
        try {
            // Check the number of parts to determine JWT type
            String[] parts = jwtString.split("\\.");
            
            if (parts.length == 5) {
                // Encrypted JWT
                return EncryptedJWT.parse(jwtString);
            } else if (parts.length == 3) {
                // Try to parse as SignedJWT first
                try {
                    return SignedJWT.parse(jwtString);
                } catch (JWTParseException e) {
                    // If SignedJWT parsing fails, try PlainJWT
                    if (jwtString.endsWith(".")) {
                        return PlainJWT.parse(jwtString);
                    } else {
                        throw e;
                    }
                }
            } else {
                throw new ParseException("Invalid JWT format: expected 3 or 5 parts, got " + parts.length);
            }
        } catch (JWTParseException e) {
            throw new ParseException("Failed to parse JWT string: " + e.getMessage());
        }
    }
    
    /**
     * Get JWT type
     */
    private static String getJWTType(JWT idToken) {
        if (idToken instanceof SignedJWT) {
            return "SignedJWT";
        } else if (idToken instanceof EncryptedJWT) {
            return "EncryptedJWT";
        } else if (idToken instanceof PlainJWT) {
            return "PlainJWT";
        } else {
            return "JWT";
        }
    }
    
    /**
     * Check if it's a standard claim
     */
    private static boolean isStandardClaim(String claimName) {
        return claimName.equals("iss") || claimName.equals("sub") || 
               claimName.equals("aud") || claimName.equals("exp") || 
               claimName.equals("iat") || claimName.equals("nbf") || 
               claimName.equals("jti");
    }
    
    /**
     * Create IDTokenClaimsSet from ID Token
     */
    public static IDTokenClaimsSet createIDTokenClaimsSet(JWT idToken) throws ParseException {
        if (idToken == null) {
            return null;
        }
        
        try {
            JWTClaimsSet jwtClaimsSet = idToken.getJWTClaimsSet();
            return new IDTokenClaimsSet(jwtClaimsSet);
        } catch (java.text.ParseException e) {
            throw new ParseException("Failed to create IDTokenClaimsSet: " + e.getMessage());
        }
    }
    
    /**
     * Extract claims directly from JSON string
     */
    public static Map<String, Object> extractClaimsFromJson(String jsonString) throws ParseException {
        try {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> tokenMap = gson.fromJson(jsonString, type);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = (Map<String, Object>) tokenMap.get("claims");
            
            if (claims == null) {
                // Try to parse from JWT string
                String jwtString = (String) tokenMap.get("jwt_string");
                if (jwtString != null) {
                    JWT idToken = parseJWTString(jwtString);
                    JWTClaimsSet claimsSet = idToken.getJWTClaimsSet();
                    
                    claims = new HashMap<>();
                    for (String claimName : claimsSet.getClaims().keySet()) {
                        claims.put(claimName, claimsSet.getClaim(claimName));
                    }
                }
            }
            
            return claims != null ? claims : new HashMap<>();
            
        } catch (Exception e) {
            throw new ParseException("Failed to extract claims from JSON: " + e.getMessage());
        }
    }
    
    /**
     * Simplified conversion: only pass JWT string
     */
    public static String idTokenToSimpleJson(JWT idToken) throws ParseException {
        if (idToken == null) {
            return null;
        }
        
        Map<String, Object> simpleMap = new HashMap<>();
        simpleMap.put("jwt", idToken.serialize());
        simpleMap.put("type", "id_token");
        
        return gson.toJson(simpleMap);
    }
    
    /**
     * Convert simplified JSON back to ID Token
     */
    public static JWT simpleJsonToIDToken(String jsonString) throws ParseException {
        try {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> simpleMap = gson.fromJson(jsonString, type);
            
            String jwtString = (String) simpleMap.get("jwt");
            if (jwtString == null || jwtString.trim().isEmpty()) {
                throw new ParseException("Missing 'jwt' field in JSON");
            }
            
            return parseJWTString(jwtString);
            
        } catch (Exception e) {
            throw new ParseException("Failed to parse simple JSON: " + e.getMessage());
        }
    }
    
    /**
     * Create a test JWT token for testing
     */
    public static JWT createTestIDToken() {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://auth.example.com")
                .subject("user123")
                .audience("client-123")
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .issueTime(new Date())
                .claim("name", "John Doe")
                .claim("email", "john.doe@example.com")
                .claim("preferred_username", "johndoe")
                .claim("auth_time", System.currentTimeMillis() / 1000)
                .build();
            
            return new PlainJWT(claimsSet);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test ID Token: " + e.getMessage());
        }
    }
    
    /**
     * Main test method
     */
    public static void main(String[] args) {
        System.out.println("=== ID Token Converter and Format Detection Test ===\n");
        
        try {
            // 1. Create a sample ID Token
            System.out.println("Test 1: Create sample ID Token");
            JWT idToken = createTestIDToken();
            System.out.println("Original ID Token: " + idToken.serialize().substring(0, 50) + "...");
            
            // 2. Test different formats
            System.out.println("\nTest 2: Format detection test");
            
            // 2.1 Full format JSON
            String fullJson = idTokenToJson(idToken);
            System.out.println("Full format JSON: " + isIDTokenJson(fullJson));
            System.out.println("Format type: " + getIDTokenJsonFormat(fullJson));
            
            // 2.2 Simplified format JSON
            String simpleJson = idTokenToSimpleJson(idToken);
            System.out.println("\nSimplified format JSON: " + isIDTokenJson(simpleJson));
            System.out.println("Format type: " + getIDTokenJsonFormat(simpleJson));
            
            // 2.3 Pure JWT string
            String pureJwt = idToken.serialize();
            System.out.println("\nPure JWT string: " + isIDTokenJson(pureJwt));
            System.out.println("Format type: " + getIDTokenJsonFormat(pureJwt));
            
            // 2.4 Invalid JSON
            String invalidJson = "{\"name\": \"test\"}";
            System.out.println("\nInvalid JSON: " + isIDTokenJson(invalidJson));
            System.out.println("Format type: " + getIDTokenJsonFormat(invalidJson));
            
            // 3. Test parsing any format
            System.out.println("\nTest 3: Parse any format");
            
            String[] testFormats = {fullJson, simpleJson, pureJwt};
            String[] formatNames = {"Full format", "Simplified format", "Pure JWT"};
            
            for (int i = 0; i < testFormats.length; i++) {
                System.out.println("\nParse " + formatNames[i] + ":");
                try {
                    JWT parsedToken = parseAnyIDTokenJson(testFormats[i]);
                    System.out.println("Parse successful: " + parsedToken.getClass().getSimpleName());
                    System.out.println("Subject: " + parsedToken.getJWTClaimsSet().getSubject());
                } catch (Exception e) {
                    System.out.println("Parse failed: " + e.getMessage());
                }
            }
            
            // 4. Test boundary cases
            System.out.println("\nTest 4: Boundary cases test");
            
            String[] edgeCases = {
                null,
                "",
                "{}",
                "not-a-json",
                "{\"jwt\": \"invalid.jwt.string\", \"type\": \"id_token\"}",
                "{\"jwt_string\": \"invalid.jwt.string\", \"header\": {}, \"claims\": {}}"
            };
            
            for (String edgeCase : edgeCases) {
                boolean isIDToken = edgeCase == null ? false : isIDTokenJson(edgeCase);
                IDTokenJsonFormat format = edgeCase == null ? 
                    IDTokenJsonFormat.NOT_ID_TOKEN : getIDTokenJsonFormat(edgeCase);
                System.out.println(String.format("Input: %-50s Is ID Token: %-5s Format: %s",
                    edgeCase != null && edgeCase.length() > 20 ? 
                        edgeCase.substring(0, 20) + "..." : edgeCase,
                    isIDToken,
                    format));
            }
            
            // 5. Test with encrypted JWT (if needed in the future)
            System.out.println("\nTest 5: Encrypted JWT format test");
            
            // Note: Creating EncryptedJWT requires encryption keys
            // This is just a placeholder for future testing
            
        } catch (ParseException e) {
            System.err.println("Conversion error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Other error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
