package com.example.demo.controller;

import com.example.demo.model.Cage;
import com.example.demo.model.Food;
import com.example.demo.model.Parrot;
import com.example.demo.repository.FoodRepository;
import com.example.demo.repository.ParrotRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;



@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/parrots")

public class ParrotController {

	@Autowired
    private ParrotRepository parrotRepository;
	
	@Autowired
    private FoodRepository foodRepository;

	
	
	
    // 1. CREATE (POST) (insert)
	// http://localhost:8080/api/parrots		+ body JSON
	
    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMINISTRATOR')")
    public Parrot createParrot(@Valid @RequestBody Parrot parrot) {
        return parrotRepository.save(parrot);
    }
    
    
    
    
    
    // 2. READ (GET by id) (select)
    // http://localhost:8080/api/parrots/{id}
    
    @GetMapping("/{id}")
    public ResponseEntity<Parrot> getParrotById(@PathVariable int id) {
        Optional<Parrot> parrot = parrotRepository.findById(id);
        return parrot.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    
    
    
    
    // 3. READ (GET all) (select) (lista con Paginazione e Filtraggio) 
    // http://localhost:8080/api/parrots (all) + ?page=0&size=5&breed=Ara (per pagination e filtering)
    
    @GetMapping
    public Page<Parrot> getAllParrots(
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer ageMonths, 
            @RequestParam(required = false) Integer birthYear, 
            Pageable pageable) {
        
        if (breed != null && gender != null) {
            return parrotRepository.findByBreedAndGender(breed, gender, pageable);
        } else if (breed != null) {
            return parrotRepository.findByBreed(breed, pageable);
        } else if (gender != null) {
            return parrotRepository.findByGender(gender, pageable);
        } else if (ageMonths != null) {
            return parrotRepository.findByAgeMonths(ageMonths, pageable);
        } else if (birthYear != null) {
            return parrotRepository.findByBirthYear(birthYear, pageable);
        } else {
            return parrotRepository.findAll(pageable);
        }
    }    
    

    
    
    
    // 4. UPDATE (PUT) (update)
    // http://localhost:8080/api/parrots/{id}		+ body JSON
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMINISTRATOR')")
    public ResponseEntity<Parrot> updateParrot(@PathVariable int id, @Valid @RequestBody Parrot parrotDetails) {
        Optional<Parrot> optionalParrot = parrotRepository.findById(id);
        
        if (optionalParrot.isPresent()) {
            Parrot parrot = optionalParrot.get();
            parrot.setRingNumber(parrotDetails.getRingNumber());
            parrot.setBreed(parrotDetails.getBreed());
            parrot.setGender(parrotDetails.getGender());
            parrot.setAgeMonths(parrotDetails.getAgeMonths());
            parrot.setBirthYear(parrotDetails.getBirthYear());
            parrot.setCage(parrotDetails.getCage());
            parrot.setFoods(parrotDetails.getFoods());
            
            Parrot updatedParrot = parrotRepository.save(parrot);
            return ResponseEntity.ok(updatedParrot);
        } else {
            return ResponseEntity.notFound().build(); //204 No Content (non ce niente da restiutire nel body della risposta) (a differenza di 204 OK quando ce qualcosa che ritorna nel body)
        }
    }
    
    
    
    
    
    // 5. UPDATE (PATCH) (update)
    // http://localhost:8080/api/parrots/{id}		+ body JSON

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMINISTRATOR')")
    public ResponseEntity<Parrot> patchParrot(@PathVariable int id, @RequestBody Parrot parrotUpdates) {
        Optional<Parrot> optionalParrot = parrotRepository.findById(id);
        
        if (optionalParrot.isPresent()) {
            Parrot parrot = optionalParrot.get();
            
            // Aggiorna solo i campi effettivamente presenti nel JSON
            if (parrotUpdates.getRingNumber() != null) parrot.setRingNumber(parrotUpdates.getRingNumber());
            if (parrotUpdates.getBreed() != null) parrot.setBreed(parrotUpdates.getBreed());
            if (parrotUpdates.getGender() != null) parrot.setGender(parrotUpdates.getGender());
            if (parrotUpdates.getAgeMonths() != null) parrot.setAgeMonths(parrotUpdates.getAgeMonths());
            if (parrotUpdates.getBirthYear() != null) parrot.setBirthYear(parrotUpdates.getBirthYear());
            if (parrotUpdates.getCage() != null) parrot.setCage(parrotUpdates.getCage());
            if (parrotUpdates.getFoods() != null) parrot.setFoods(parrotUpdates.getFoods());
            
            Parrot updatedParrot = parrotRepository.save(parrot);
            return ResponseEntity.ok(updatedParrot);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    

    
    
    // 6. DELETE (DELETE) (delete)
    // http://localhost:8080/api/parrots/{id}
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteParrot(@PathVariable int id) {
        if (parrotRepository.existsById(id)) {
            parrotRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    

    
    
    // 7. METODO COMBINATO: PROFILO MULTI-ENTITÀ
    // Requisito: API che restituisce una risorsa combinata da più entità (almeno 2 o 3)
    // Combina: Dati del Pappagallo + Gabbia associata + Cibi associati
    // http://localhost:8080/api/parrots/{id}/details
    
    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getParrotCombinedProfile(@PathVariable int id) {
        Optional<Parrot> optionalParrot = parrotRepository.findById(id);
        
        if (optionalParrot.isPresent()) {
            Parrot parrot = optionalParrot.get();
            
            Map<String, Object> combinedProfile = new HashMap<>();
            combinedProfile.put("parrotInfo", parrot);
            combinedProfile.put("assignedCage", parrot.getCage()); // Entità Cage collegata
            combinedProfile.put("dietaryPlan", parrot.getFoods()); // Entità Food collegate (Molti a Molti)
            
            return ResponseEntity.ok(combinedProfile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    // 8. METODO GERARCHICO (Hierarchical Endpoint) (DOPPIO)
    // Restituisce la gabbia di un determinato pappagallo
    // http://localhost:8080/api/parrots/{id}/cage
    
    @Transactional
    @GetMapping("/{id}/cage")
    public ResponseEntity<Cage> getCageByParrot(@PathVariable int id) {
        Optional<Parrot> optionalParrot = parrotRepository.findById(id);
        if (optionalParrot.isEmpty()) return ResponseEntity.notFound().build();
        
        Cage cage = optionalParrot.get().getCage();
        if (cage == null) return ResponseEntity.noContent().build();
        
        return ResponseEntity.ok(cage);
    }

    
    
    // 9. METODO GERARCHICO (Hierarchical Endpoint) (DOPPIO)
    // Restiutisce tutti i cibi di un determinato pappagallo
    // http://localhost:8080/api/parrots/{id}/foods
    
    @Transactional
    @GetMapping("/{id}/foods")
    public ResponseEntity<List<Food>> getFoodsByParrot(@PathVariable int id) {
        Optional<Parrot> optionalParrot = parrotRepository.findById(id);
        if (optionalParrot.isEmpty()) return ResponseEntity.notFound().build();
        
        List<Food> foods = new ArrayList<>(optionalParrot.get().getFoods());
        return ResponseEntity.ok(foods);
    }

    
    
    // 10. METODO GERARCHICO (Hierarchical Endpoint) (TRIPLO) 
    // Restituisce tutte le gabbie in cui ce un determinato cibo, solo se un determinato pappagallo lo mangia
    // http://localhost:8080/api/parrots/{id}/foods/{id}/cages
    
    @Transactional
    @GetMapping("/{pId}/foods/{fId}/cages")
    public ResponseEntity<List<Cage>> getCagesForParrotFood(@PathVariable int pId, @PathVariable int fId) {
        Optional<Parrot> optionalParrot = parrotRepository.findById(pId);
        if (optionalParrot.isEmpty()) return ResponseEntity.notFound().build();
        
        // Verifica che il pappagallo mangi effettivamente quel cibo
        boolean eatsFood = false;
        for (Food f : optionalParrot.get().getFoods()) {
            if (f.getId() == fId) eatsFood = true;
        }
        if (!eatsFood) return ResponseEntity.notFound().build();
        
        // Se lo mangia, carica il cibo per ottenere le sue gabbie
        Optional<Food> optionalFood = foodRepository.findById(fId);
        if (optionalFood.isEmpty()) return ResponseEntity.notFound().build();
        
        List<Cage> cages = new ArrayList<>(optionalFood.get().getCages());
        return ResponseEntity.ok(cages);
    }
    
    
    
    // 11. METODO GERARCHICO (Hierarchical Endpoint) (TRIPLO) ???logica url???
    // Restituisce tutti i cibi che mangia un determinato pappagallo, solo se si trova in una determinata gabbia (la sua) (come il metodo di Cage getFoodsByCageAndParrot)
    // http://localhost:8080/api/parrots/{id}/cage/{id}/foods
    
    @Transactional
    @GetMapping("/{parrotId}/cage/{cageId}/foods")
    public ResponseEntity<List<Food>> getFoodsByParrotAndCage(@PathVariable int parrotId, @PathVariable int cageId) {
        
        // 1. Cercamo il pappagallo nel database
        Optional<Parrot> optionalParrot = parrotRepository.findById(parrotId);
        
        if (optionalParrot.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404 Not Found se il pappagallo non esiste
        }
        
        Parrot parrot = optionalParrot.get();
        
        // 2. VERIFICA GERARCHICA: Il pappagallo ha una gabbia ed è proprio la gabbia specificata nell'URL?
        if (parrot.getCage() == null || parrot.getCage().getId() != cageId) {
            // Se il pappagallo non ha una gabbia o si trova in una gabbia DIVERSA da quella nell'URL
            return ResponseEntity.notFound().build(); // 404 Not Found per inerenza di gerarchia errata
        }
        
        // 3. Se la gerarchia è valida, restituiamo la lista dei cibi associati a quel pappagallo
        return ResponseEntity.ok(parrot.getFoods());
    }
}