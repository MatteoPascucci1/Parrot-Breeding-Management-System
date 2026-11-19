package com.example.demo.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Role;
import com.example.demo.model.RoleType;
import com.example.demo.model.User;
import com.example.demo.payload.MessageResponse;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;




@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminController {

	
	
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    
    
    
    
   
    
    // 1.  READ (GET all) (select)									Visualizza tutti gli utenti che hanno chiesto di diventare MEMBER
    // http://localhost:8080/api/admin/pending-members
    
    @GetMapping("/pending-members")
    public List<User> getPendingMembers() {
    	
        return userRepository.findByMemberPendingTrue();
    }

   
    
    // 2. UPDATE (PUT) (update)										Approva un utente e promuovilo a ROLE_MEMBER
    // http://localhost:8080/api/admin/approve-member/{userId}
    
    @PutMapping("/approve-member/{userId}")
    public ResponseEntity<?> approveMember(@PathVariable Long userId) {
    	
    	Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();

        // Impedisce di alterare un account ADMINISTRATOR
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ROLE_ADMINISTRATOR);
        if (isAdmin) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cannot modify the role of an ADMINISTRATOR."));
        }

        // Approva solo utenti GUEST con richiesta MEMBER effettivamente in sospeso
        if (!user.isMemberPending()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: User " + user.getUsername() + " does not have a pending MEMBER request."));
        }

        Role memberRole = roleRepository.findByName(RoleType.ROLE_MEMBER)
                .orElseThrow(() -> new RuntimeException("Error: Role MEMBER not found."));

        Set<Role> updatedRoles = new HashSet<>();
        updatedRoles.add(memberRole);

        user.setRoles(updatedRoles);
        user.setMemberPending(false);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User " + user.getUsername() + " approved as MEMBER!"));
    }
    
    
   
    // 3. DELETE (DELETE) (delete)								Elimina un utente
    // http://localhost:8080/api/admin/users/{userId}
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
    	
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(userId);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully."));
    }
    
    
    
}