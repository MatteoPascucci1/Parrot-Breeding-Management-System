package com.example.demo.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;




public class AuthTokenFilter extends OncePerRequestFilter { //controllo eseguito una sola volta per ogni richiesta HTTP (grazie a estenzione di OncePerRequestFilter)

	
	//1) Cerca l'header Authorization nella richiesta e verifica che inizi con "Bearer " (estrapolando solo la stringa del token).
	//2) Passa il token a JwtUtils per verificare che la firma sia autentica e non sia scaduto.
	//3) Estrae lo username dal token, carica i dati dell'utente e i suoi ruoli dal database tramite UserDetailsServiceImpl, e li registra nel SecurityContextHolder (la memoria temporanea della singola richiesta in Spring Security) così che i controller sappiano chi sta facendo la chiamata e con quali permessi.
	
	
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
   

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Impossibile impostare l'autenticazione utente: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    
    
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
    
    
    
    
}