/*
package JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;

import java.security.Key;
import java.util.Date;

@ApplicationScoped
public class JwtUtil {

    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 86400000L; // 24 ore in millisecondi
    private static final String ISSUER = "your-application"; // Il nome dell'applicazione come issuer del token

    public String generateToken(String username) {
        // Crea il token JWT impostando i vari claims
        return Jwts.builder()
                .setIssuer(ISSUER)  // Aggiungi l' issuer
                .setSubject(username)  // Aggiungi il subject (username dell'utente)
                .setIssuedAt(new Date())  // Data di emissione
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // Data di scadenza
                .signWith(SECRET_KEY)  // Firma il token con la chiave segreta
                .compact();  // Restituisci il token compattato
    }


    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);  // Ottieni le claims
            // Verifica se l' issuer del token è valido
            if (!claims.getIssuer().equals(ISSUER)) {
                throw new IllegalArgumentException("Invalid token issuer");
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
*/
