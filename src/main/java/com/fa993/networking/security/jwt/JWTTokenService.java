package com.fa993.networking.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.compression.GzipCompressionCodec;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.jsonwebtoken.impl.TextCodec.BASE64;
import static java.util.Objects.requireNonNull;

@Service
public class JWTTokenService implements TokenService {

    private static final String DOT = ".";
    private static final GzipCompressionCodec COMPRESSION_CODEC = new GzipCompressionCodec();

    private Clock cl;

    private String issuer;
    private int expirationSec;
    private int clockSkewSec;
    private String secretKey;

    public JWTTokenService() {
        this("manga-server", 3600, 5, "mySecrecy");
    }

    public JWTTokenService(String issuer, int expirationSec, int clockSkewSec, String secretKey) {
        super();
        this.issuer = requireNonNull(issuer);
        this.expirationSec = requireNonNull(expirationSec);
        this.clockSkewSec = requireNonNull(clockSkewSec);
        this.secretKey = BASE64.encode(requireNonNull(secretKey));
        this.cl = new Clock() {
            @Override
            public Date now() {
                return Date.from(Instant.now());
            }
        };
    }

    @Override
    public String permanent(Map<String, String> attributes) {
        return newToken(attributes, 0);
    }

    @Override
    public String expiring(Map<String, String> attributes) {
        return newToken(attributes, expirationSec);
    }

    private String newToken(Map<String, String> attributes, int expiresInSec) {
        Instant t = Instant.now();
        Claims claims = Jwts.claims().setIssuer(issuer).setIssuedAt(Date.from(t));

        if (expiresInSec > 0) {
            Instant t2 = t.plusSeconds(expiresInSec);
            claims.setExpiration(Date.from(t2));
        }

        claims.putAll(attributes);

        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS256, secretKey).compressWith(COMPRESSION_CODEC).compact();
    }

    @Override
    public Map<String, String> verify(String token) {
        JwtParser parser = Jwts.parser().requireIssuer(issuer).setClock(cl).setAllowedClockSkewSeconds(clockSkewSec).setSigningKey(secretKey);
        return parseClaims(() -> parser.parseClaimsJws(token).getBody());
    }

    @Override
    public Map<String, String> untrusted(String token) {
        JwtParser parser = Jwts.parser().requireIssuer(issuer).setClock(cl).setAllowedClockSkewSeconds(clockSkewSec).setSigningKey(secretKey);
        String withoutSignature = substringBeforeLast(token, DOT) + DOT;
        return parseClaims(() -> parser.parseClaimsJwt(withoutSignature).getBody());
    }

    private static Map<String, String> parseClaims(Supplier<Claims> toClaims) {
        try {
            Claims claims = toClaims.get();
            Map<String, String> m = new HashMap<>();
            for(Map.Entry<String, Object> e : claims.entrySet()) {
                m.put(e.getKey(), String.valueOf(e.getValue()));
            }
            return m;
        } catch (IllegalArgumentException | JwtException ex) {
            return new HashMap<>();
        }
    }

    private String substringBeforeLast(String str, String seperator) {
        int indexOf = str.lastIndexOf(seperator);
        if (indexOf < 0) {
            return str;
        } else {
            return str.substring(0, indexOf);
        }
    }

}
