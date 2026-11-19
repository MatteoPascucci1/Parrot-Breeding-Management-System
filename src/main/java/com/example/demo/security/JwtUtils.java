package com.example.demo.security;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;



//Questa classe si occupa materialmente di creare la stringa JWT (Header + Payload + Signature) quando l'utente fa login, e di verificarne l'autenticità e la scadenza quando arriva una richiesta.
 
//Header: Specifica l'algoritmo usato per la firma (HS256) e il tipo di token (JWT). 
//Payload (Claims): Contiene i dati dell'utente autenticato, in particolare il subject (sub, cioè lo username), la data di emissione (iat) e la scadenza (exp).   
//Signature (Firma): È il sigillo matematico calcolato combinando Header, Payload e il tuo jwtSecret.   

@Component
public class JwtUtils {
	
	
	
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${parrot.app.jwtSecret}")
    private String jwtSecret;

    @Value("${parrot.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    
    
    // 1. Genera il token JWT (una stringa) partendo dall'utente autenticato, quando l'utente fa login
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
    

    // 2. Estrae lo username leggendo il Payload del token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    
    // 3. Controlla che il token non sia scaduto o falsificato. Quando un utente dopo essersi loggato ritorna per chiamare un API, ricalcola la firma (Signature) usando jwtSecret. Se qualcuno ha provato a manomettere il Payload (ad esempio alterando lo username o i permessi), la firma non combacia più e il server respinge la richiesta.
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT Token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT Token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token not supported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Empty JWT payload string: {}", e.getMessage());
        }
        return false;
    }
    
    
    
    
}