package com.example.javawebspringbootsecurity.security;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenUtils {

    //@Value ---> govori springu ovoj promenljivoj ce biti ta vrednost koja je u zagradi u @Value()
    @Value("myXAuthSecret")
    private String secret; //Enkriptovanje uvek se nesto sifruje, neki tekst... Username i pw... Dodaje se neki kljuj tome i to je taj secret... I ako znas taj kljuc, mozes da pretvoris tekst u sifru, i sifru
    // u tekst

    @Value("18000") //in seconds (5 hours)
    private Long expiration; // nakon nekog vremena, token prestaje da vazi...

    public String getUsernameFromToken(String token) { //funkcija koja vraca username nekog korisnika na osnovu nekog tokena
        String username;
        try {
            Claims claims = this.getClaimsFromToken(token); //Claims je tvrdnja, uzmemo username i pw i zapakujemo ga u Claims
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(this.secret)
                    .parseClaimsJws(token).getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = this.getExpirationDateFromToken(token);
        return expiration.before(new Date(System.currentTimeMillis()));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("roles", userDetails.getAuthorities());
        claims.put("sub", userDetails.getUsername());
        claims.put("created", new Date(System.currentTimeMillis()));
        return Jwts.builder().setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }
}