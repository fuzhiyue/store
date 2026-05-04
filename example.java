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
import com.google.gson.reflect.TypeToken;
import java.text.ParseException as JWTParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Type;

/**
 * ID Token 转换工具类
 * 处理 ID Token 与 JSON 字符串之间的相互转换
 */
public class IDTokenConverter {
    
    private static final Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create();
    
    /**
     * 将 ID Token 转换为 JSON 字符串
     */
    public static String idTokenToJson(JWT idToken) throws ParseException {
        if (idToken == null) {
            return null;
        }
        
        try {
            Map<String, Object> tokenMap = new HashMap<>();
            
            // 1. 基本信息
            tokenMap.put("jwt_string", idToken.serialize());
            tokenMap.put("jwt_type", getJWTType(idToken));
            
            // 2. 头部信息
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
            
            // 3. 声明信息
            JWTClaimsSet claimsSet = idToken.getJWTClaimsSet();
            Map<String, Object> claimsMap = new HashMap<>();
            
            // 标准声明
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
            
            // 自定义声明
            for (String claimName : claimsSet.getClaims().keySet()) {
                if (!isStandardClaim(claimName)) {
                    claimsMap.put(claimName, claimsSet.getClaim(claimName));
                }
            }
            
            tokenMap.put("claims", claimsMap);
            
            // 4. 签名信息（仅对 SignedJWT）
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
     * 从 JSON 字符串恢复 ID Token
     */
    public static JWT jsonToIDToken(String jsonString) throws ParseException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        
        try {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> tokenMap = gson.fromJson(jsonString, type);
            
            // 检查是否包含 JWT 字符串
            String jwtString = (String) tokenMap.get("jwt_string");
            if (jwtString != null && !jwtString.trim().isEmpty()) {
                // 直接解析 JWT 字符串
                return parseJWTString(jwtString);
            }
            
            // 如果没有 JWT 字符串，尝试从各部分重建
            // 注意：实际中不建议这样做，因为签名无法重建
            throw new ParseException("JWT string is required for reconstruction");
            
        } catch (Exception e) {
            throw new ParseException("Failed to convert JSON to ID Token: " + e.getMessage());
        }
    }
    
    /**
     * 解析 JWT 字符串
     */
    private static JWT parseJWTString(String jwtString) throws ParseException {
        try {
            // 尝试解析为 SignedJWT
            if (jwtString.split("\\.").length == 3) {
                if (jwtString.endsWith(".")) {
                    // Plain JWT
                    return PlainJWT.parse(jwtString);
                } else {
                    // Signed JWT
                    return SignedJWT.parse(jwtString);
                }
            } else {
                // Encrypted JWT
                return EncryptedJWT.parse(jwtString);
            }
        } catch (JWTParseException e) {
            throw new ParseException("Failed to parse JWT string: " + e.getMessage());
        }
    }
    
    /**
     * 获取 JWT 类型
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
     * 检查是否为标准声明
     */
    private static boolean isStandardClaim(String claimName) {
        return claimName.equals("iss") || claimName.equals("sub") || 
               claimName.equals("aud") || claimName.equals("exp") || 
               claimName.equals("iat") || claimName.equals("nbf") || 
               claimName.equals("jti");
    }
    
    /**
     * 从 ID Token 创建 IDTokenClaimsSet
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
     * 从 JSON 字符串直接提取声明信息
     */
    public static Map<String, Object> extractClaimsFromJson(String jsonString) throws ParseException {
        try {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> tokenMap = gson.fromJson(jsonString, type);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = (Map<String, Object>) tokenMap.get("claims");
            
            if (claims == null) {
                // 尝试从 JWT 字符串解析
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
     * 简化转换：只传递 JWT 字符串
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
     * 从简化 JSON 恢复 ID Token
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
     * 测试方法
     */
    public static void main(String[] args) {
        System.out.println("=== ID Token 转换测试 ===\n");
        
        try {
            // 1. 创建一个示例 ID Token
            System.out.println("测试1: 创建示例 ID Token");
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://auth.example.com")
                .subject("user123")
                .audience("client-123")
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .issueTime(new Date())
                .claim("name", "张三")
                .claim("email", "zhangsan@example.com")
                .claim("preferred_username", "zhangsan")
                .claim("auth_time", System.currentTimeMillis() / 1000)
                .build();
            
            // 创建 PlainJWT（示例，实际中应该是 SignedJWT）
            PlainJWT idToken = new PlainJWT(claimsSet);
            System.out.println("原始 ID Token: " + idToken.serialize());
            
            // 2. 转换为 JSON
            System.out.println("\n测试2: 转换为 JSON");
            String json = idTokenToJson(idToken);
            System.out.println("JSON 字符串:");
            System.out.println(json);
            
            // 3. 从 JSON 恢复
            System.out.println("\n测试3: 从 JSON 恢复");
            JWT restoredToken = jsonToIDToken(json);
            System.out.println("恢复的 JWT: " + restoredToken.serialize());
            
            // 4. 提取声明信息
            System.out.println("\n测试4: 提取声明信息");
            IDTokenClaimsSet idTokenClaims = createIDTokenClaimsSet(restoredToken);
            System.out.println("Subject: " + idTokenClaims.getSubject());
            System.out.println("Issuer: " + idTokenClaims.getIssuer());
            System.out.println("Audience: " + idTokenClaims.getAudience());
            System.out.println("Name: " + idTokenClaims.getStringClaim("name"));
            System.out.println("Email: " + idTokenClaims.getStringClaim("email"));
            System.out.println("Auth Time: " + idTokenClaims.getLongClaim("auth_time"));
            
            // 5. 简化转换测试
            System.out.println("\n测试5: 简化转换");
            String simpleJson = idTokenToSimpleJson(idToken);
            System.out.println("简化 JSON: " + simpleJson);
            
            JWT fromSimpleJson = simpleJsonToIDToken(simpleJson);
            System.out.println("从简化 JSON 恢复: " + fromSimpleJson.serialize());
            
            // 6. 验证一致性
            System.out.println("\n测试6: 验证一致性");
            boolean consistent = idToken.serialize().equals(restoredToken.serialize());
            System.out.println("原始和恢复的 Token 是否一致: " + consistent);
            
        } catch (ParseException e) {
            System.err.println("转换错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("其他错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
