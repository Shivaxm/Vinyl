package com.shivam.store.services;



import com.shivam.store.config.JwtConfig;
import com.shivam.store.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@AllArgsConstructor
public class JwtService {
    private final JwtConfig jwtConfig;

    public Jwt generateAccessToken(User user) {

        return generateToken(user, jwtConfig.getAccessExpiration());
    }

    public Jwt generateRefreshToken(User user) {
        final long EXPIRATION_TIME = 604800;
        return generateToken(user, jwtConfig.getRefreshExpiration());
    }

    private Jwt generateToken(User user, long tokenExpiration) {
        UUID id = user.getId();

        var claims = Jwts.claims().subject(user.getId().toString())
                .add("email", user.getEmail())
                .add("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .build();
        return new Jwt(jwtConfig.getSecretKey(), claims);
    }

    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(jwtConfig.getSecretKey(),  claims);
        }  catch (JwtException e) {
            return null;
        }
    }


    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
