package com.example.demo.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Role;
import com.example.demo.model.RoleType;
import com.example.demo.model.User;
import com.example.demo.payload.JwtResponse;
import com.example.demo.payload.LoginRequest;
import com.example.demo.payload.MessageResponse;
import com.example.demo.payload.SignupRequest;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtils;
import com.example.demo.security.UserDetailsImpl;




@CrossOrigin(origins = "*", maxAge = 3600) //origins = "*" indica accetta richieste da chiunque, maxAge = 3600 serve per q	uando un browser deve inviare dati complessi (come un JSON in POST per il login), prima di mandare la richiesta vera invia una chiamata silenziosa di controllo (detta preflight OPTIONS) per chiedere al server se il CORS è attivo. Il valore 3600 (espresso in secondi, pari a 1 ora) dice al browser: "Memorizza questa risposta positiva per un'ora intera, così non dovrai ripetere il controllo prima di ogni singola chiamata".
@RestController
@RequestMapping("/api/auth")
public class AuthController { //Questo controller espone due endpoint pubblici (lasciati aperti in WebSecurityConfig tramite /api/auth/**):

	
	
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;
    
    
    
    
    
    // 1. CREATE (POST) (insert)
    // http://localhost:8080/api/auth/login

    @PostMapping("/login") //passa username e password (LoginRequest) all'AuthenticationManager di Spring Security; se le credenziali sono corrette, genera il token con JwtUtils e lo restituisce al client insieme all'ID, allo username e alla lista dei ruoli (JwtResponse).
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles));
    }
    
    
    
    // 2. CREATE (POST) (insert)
    // http://localhost:8080/api/auth/register

    @PostMapping("/register") //riceve username, password e ruolo (SignupRequest), controlla che lo username non sia già occupato, cripta la password con BCryptPasswordEncoder, associa i ruoli scelti tramite RoleType (assegnando ROLE_GUEST di default se non viene specificato nulla) e salva l'utente sul database MySQL.
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
    	
    	
    	if (signUpRequest.getUsername() == null || signUpRequest.getUsername().isBlank() ||
    	        signUpRequest.getPassword() == null || signUpRequest.getPassword().isBlank()) {
    	    return ResponseEntity.badRequest().body(new MessageResponse("Error: Username and password are mandatory!"));
    	}

    	if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
    	    return ResponseEntity.badRequest().body(new MessageResponse("Error: Username already in use!"));
    	}

    	User user = new User();
    	user.setUsername(signUpRequest.getUsername());
    	user.setPassword(encoder.encode(signUpRequest.getPassword()));

    	Set<String> strRoles = signUpRequest.getRole();
    	Set<Role> roles = new HashSet<>();

    	if (strRoles == null || strRoles.isEmpty()) {
    	    // Se il campo "role" non viene inviato, assegna di default ROLE_GUEST
    	    Role guestRole = roleRepository.findByName(RoleType.ROLE_GUEST)
    	            .orElseThrow(() -> new RuntimeException("Error: GUEST role not found in the database."));
    	    roles.add(guestRole);
    	} else {
    	    for (String role : strRoles) {
    	        if (role == null) {
    	            return ResponseEntity.badRequest()
    	                    .body(new MessageResponse("Error: Role value cannot be null. Allowed values: guest, member, admin."));
    	        }

    	        switch (role.toLowerCase()) {
    	            case "guest":
    	                Role guestRole = roleRepository.findByName(RoleType.ROLE_GUEST)
    	                        .orElseThrow(() -> new RuntimeException("Error: Role GUEST not found."));
    	                roles.add(guestRole);
    	                break;

    	            case "member":
    	                // Assegna temporaneamente GUEST e mette in attesa di approvazione Admin
    	                Role guestForMember = roleRepository.findByName(RoleType.ROLE_GUEST)
    	                        .orElseThrow(() -> new RuntimeException("Error: Role GUEST not found."));
    	                roles.add(guestForMember);
    	                user.setMemberPending(true);
    	                break;

    	            case "admin":
    	                Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMINISTRATOR)
    	                        .orElseThrow(() -> new RuntimeException("Error: Role ADMINISTRATOR not found."));
    	                roles.add(adminRole);
    	                break;

    	            default:
    	                return ResponseEntity.badRequest()
    	                        .body(new MessageResponse("Error: Invalid role '" + role + "'. Allowed values are only: guest, member, admin."));
    	        }
    	    }
    	}

    	user.setRoles(roles);
    	userRepository.save(user);

    	if (user.isMemberPending()) {
    	    return ResponseEntity.ok(new MessageResponse(
    	            "Registered as GUEST! Your request to become a MEMBER is pending administrator approval."));
    	}

    	return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    
    
    
}