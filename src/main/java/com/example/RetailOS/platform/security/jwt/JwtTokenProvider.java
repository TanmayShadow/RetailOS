package com.example.RetailOS.platform.security.jwt;

import com.example.RetailOS.platform.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes =
                Decoders.BASE64.decode(jwtProperties.getSecretKey());

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserPrincipal user) {

        return Jwts.builder()
                .subject(user.getUsername())

                .claim(JwtClaims.USER_ID,
                        user.getUserId().toString())

                .claim(JwtClaims.SHOP_ID,
                        user.getShopId().toString())

                .claim(JwtClaims.ROLE,
                        user.getRole().name())

                .claim(JwtClaims.TOKEN_TYPE,
                        TokenType.ACCESS.name())

                .issuedAt(new Date())

                .expiration(new Date(
                        System.currentTimeMillis()
                                + jwtProperties.getAccessTokenExpiration()))

                .signWith(getSigningKey())

                .compact();
    }

    public String generateRefreshToken(UserPrincipal user) {

        return Jwts.builder()
                .subject(user.getUsername())

                .claim(JwtClaims.USER_ID,
                        user.getUserId().toString())

                .claim(JwtClaims.TOKEN_TYPE,
                        TokenType.REFRESH.name())

                .issuedAt(new Date())

                .expiration(new Date(
                        System.currentTimeMillis()
                                + jwtProperties.getRefreshTokenExpiration()))

                .signWith(getSigningKey())

                .compact();
    }

    public String extractUsername(String token) {

        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token) {

        return Jwts.parser()

                .verifyWith(getSigningKey())

                .build()

                .parseSignedClaims(token)

                .getPayload();
    }

    public boolean isTokenValid(String token,
                                UserDetails userDetails) {

        String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {

        return extractClaims(token)

                .getExpiration()

                .before(new Date());
    }
}
