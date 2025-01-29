
package JWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import java.util.Optional;

@ApplicationScoped
public class JwtUtil {

    private static final String ISSUER = "EasyTask"; // Nome della tua app
    private static final long EXPIRATION_TIME = 86400000L; // 24 ore (in millisecondi)

    private final Key SECRET_KEY;

    public JwtUtil() {
        this.SECRET_KEY = loadSecretKey(); // 🔹 Ora viene inizializzato solo quando CDI crea l'istanza
    }

    private Key loadSecretKey() {
        String secret = Optional.ofNullable(System.getenv("JWT_SECRET_KEY"))
                .orElse(System.getProperty("JWT_SECRET_KEY"));

        if (secret == null || secret.isEmpty()) {
            throw new RuntimeException("JWT_SECRET_KEY non è impostata!");
        }

        // Usa direttamente i byte della stringa UTF-8 senza Base64
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new RuntimeException("JWT_SECRET_KEY deve essere almeno 32 byte per HS256!");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }


    // 🔹 Genera un token JWT con HS256
    public String generateToken(String username) {
        return Jwts.builder()
                .setIssuer(ISSUER)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔹 Valida il token JWT
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            if (claims.getExpiration().before(new Date())) {
                throw new IllegalArgumentException("Token expired");
            }
            if (!claims.getIssuer().equals(ISSUER)) {
                throw new IllegalArgumentException("Invalid token issuer");
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 🔹 Estrai le informazioni dal token JWT
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
