package com.project.shopapp.components;

import com.project.shopapp.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JWTTokenUtil {
    @Value("${jwt.expiration}")
    private int expiration;
    @Value("${jwt.secretKey}")
    private String secretKey;

    public String generateToken(User user) throws Exception{
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("email", user.getEmail());
        claims.put("userId", user.getId());
        claims.put("roleId", user.getRole().getId());
        String subject = getSubject(user);
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        }
        catch (Exception e) {
            throw new InvalidParameterException("Can not create jwt token " + e.getMessage());
        }
    }

    private String getSubject(User user){
        if(!Objects.equals(user.getGoogleAccountId(), "")) return user.getGoogleAccountId();
        if(!Objects.equals(user.getFacebookAccountId(), "")) return user.getFacebookAccountId();
        return user.getPhoneNumber();
    }

    private Key getSignInKey() {
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public  <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = this.extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractPhoneNumber(String token) {
        return extractClaim(token, claims -> claims.get("phoneNumber", String.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public Long extractRoleId(String token) {
        return extractClaim(token, claims -> claims.get("roleId", Long.class));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String subject = extractSubject(token);
        return (subject.equals(userDetails.getUsername()))
                && !isTokenExpired(token); //check hạn của token
    }
}
