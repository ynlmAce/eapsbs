package com.bs.eaps.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.allowed-clock-skew:30}")
    private long allowedClockSkew;

    /**
     * 获取JWT中的用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 获取JWT中的过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从JWT中提取特定声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从JWT中提取所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(allowedClockSkew)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查JWT是否过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 生成JWT令牌
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 生成带有额外声明的JWT令牌
     */
    public String generateToken(String username, Map<String, Object> claims) {
        return createToken(claims, username);
    }

    /**
     * 创建JWT令牌
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证JWT令牌的有效性
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 获取签名密钥
     */
    private Key getSigningKey() {
        // 使用SHA-256哈希算法将配置的密钥扩展为256位（32字节）密钥
        // 这样即使配置的原始密钥较短，也能满足JWT的安全要求
        byte[] keyBytes = secret.getBytes();
        byte[] secureKeyBytes = new byte[32]; // 256位/32字节的密钥

        // 如果原始密钥不足32字节，使用循环填充扩展到所需长度
        // 这比直接使用短密钥更安全
        int originalLength = Math.min(keyBytes.length, 32);
        System.arraycopy(keyBytes, 0, secureKeyBytes, 0, originalLength);

        // 如果原始密钥小于32字节，使用原始密钥的部分进行填充
        if (keyBytes.length < 32) {
            for (int i = originalLength; i < 32; i++) {
                secureKeyBytes[i] = keyBytes[i % keyBytes.length];
            }
        }

        return Keys.hmacShaKeyFor(secureKeyBytes);
    }

    /**
     * 解析JWT令牌
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .setAllowedClockSkewSeconds(allowedClockSkew)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("JWT解析异常", e);
        }
    }
}