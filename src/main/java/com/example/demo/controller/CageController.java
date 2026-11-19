package com.example.demo.controller;

import com.example.demo.model.Cage;
import com.example.demo.model.Food;
import com.example.demo.model.Parrot;
import com.example.demo.repository.CageRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional; 



@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cages")

public class CageController {

    @Autowired
    private CageRepository cageRepository;

    
   
    
    // 1. CREATE (POST) (insert)
	// http://localhost:8080/api/cages		+ body JSON
    
    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMINISTRATOR')")
    public Cage createCage(@Valid @RequestBody Cage cage) {
        return cageRepository.save(cage);
    }
    
    
    
    
    
    // 2. READ (GET) (select)
    // http://localhost:8080/api/cages/{id}
    
    @GetMapping("/{id}")
    public ResponseEntity<Cage> getCageById(@PathVariable int id) {
        Optional<Cage> cage = cageRepository.findById(id);
        return cage.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    
    
    
    
    // 3.  READ (GET all) (select) (lista con Paginazione e Filtraggio) 
    // http://localhost:8080/api/cages (all) + ?page=0&size=5&cageName=A1 (per pagination e filtering)

    @GetMapping
    public Page<Cage> getAllCages(
            @RequestParam(required = false) String cageName, //filtering con Containing, cioe trova parole che contengono cageName
            Pageable pageable) {
        
        if (cageName != null && !cageName.isEmpty()) {
            return cageRepository.findByCageNameContaining(cageName, pageable);
        } else {
            return cageRepository.findAll(pageable);
        }
    }

    
    
    

    // 4. UPDATE (PUT) (update)
    // http://localhost:8080/api/cages/{id}		+ body JSON
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMINISTRATOR')")
    public ResponseEntity<Cage> updateCage(@PathVariable int id, @Valid @RequestBody Cage cageDetails) {
        Optional<Cage> optionalCage = cageRepository.findById(id);
        
        if (optionalCage.isPresent()) {
            Cage cage = optionalCage.get();
            cage.setCageName(cageDetails.getCageName());
            cage.setMaxCapacity(cageDetails.getMaxCapacity());
            cage.setFoods(cageDetails.getFoods());
            
            Cage updatedCage = cageRepository.save(cage);
            return ResponseEntity.ok(updatedCage);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    
    
    
    // 5. UPDATE (PATCH) (update)
    // http://localhost:8080/api/cages/{id}		+ body JSON
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMINISTRATOR')")
    public ResponseEntity<Cage> patchCage(@PathVariable int id, @RequestBody Cage cageUpdates) {
        Optional<Cage> optionalCage = cageRepository.findById(id);
        
        if (optionalCage.isPresent()) {
            Cage cage = optionalCage.get();
            
            // Aggiorna solo i campi effettivamente presenti nel JSON
            if (cageUpdates.getCageName() != null) cage.setCageName(cageUpdates.getCageName());
            if (cageUpdates.getMaxCapacity() != null) cage.setMaxCapacity(cageUpdates.getMaxCapacity());
            if (cageUpdates.getFoods() != null) cage.setFoods(cageUpdates.getFoods());
            
            Cage updatedCage = cageRepository.save(cage);
            return ResponseEntity.ok(updatedCage);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    
    
    
    
    // 6. DELETE (DELETE) (delete)
    // http://localhost:8080/api/cages/{id}
    
    @Transactional //serve per annullare tutte le modifiche fatte nel metodo finora se ce un errore, rileva cambiamenti alle entita collegate e genere automaticamente le query di modifica 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteCage(@PathVariable int id) {
        Optional<Cage> optionalCage = cageRepository.findById(id);

        if (optionalCage.isPresent()) {
            Cage cage = optionalCage.get();

            // per settare cage = null in ogni parrot che si trova nella gabbia cancellata
            for (Parrot parrot : cage.getParrots()) {
                parrot.setCage(null);
            }

            cageRepository.delete(cage);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }  
    
    
    
    
    
    
    
    
    
    
    
    // 7. METODO GERARCHICO (Hierarchical Endpoint) (DOPPIO)
    // Restituisce tutti i pappagalli di una determinata gabbia
    // http://localhost:8080/api/cages/{id}/parrots
    
    @Transactional
    @GetMapping("/{cageId}/parrots")
    public ResponseEntity<List<Parrot>> getParrotsInCage(@PathVariable int cageId) {
        Optional<Cage> optionalCage = cageRepository.findById(cageId);

        if (optionalCage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Usiamo new ArrayList per bypassare il Lazy Loading esattamente come hai fatto prima
        List<Parrot> pappagalliPresenti = new ArrayList<>(optionalCage.get().getParrots());

        return ResponseEntity.ok(pappagalliPresenti);
    }
    
    
    
    // 8. METODO GERARCHICO (Hierarchical Endpoint) (DOPPIO)
    // Restituisce tutti i cibi di una determinata gabbia
    // http://localhost:8080/api/cages/{id}/foods   
    
    @Transactional
    @GetMapping("/{cageId}/foods")
    public ResponseEntity<List<Food>> getFoodsByCage(@PathVariable int cageId) {
        Optional<Cage> optionalCage = cageRepository.findById(cageId);

        if (optionalCage.isEmpty()) {
            return ResponseEntity.notFound().build(); // Restituisce 404 se la gabbia non esiste
        }

        // Forza la lettura dei dati dal database in una nuova lista per bypassare il Lazy Loading
        List<Food> cibiPresenti = new ArrayList<>(optionalCage.get().getFoods());

        return ResponseEntity.ok(cibiPresenti);
    }
    
    
    
    // 9. METODO GERARCHICO (Hierarchical Endpoint) (TRIPLO)
    // Restituisce tutti i cibi di una determinato pappagallo, solo se esso appartiene a una determinata gabbia (come metodo di Parrot getFoodByParrotAndCage)
    // http://localhost:8080/api/cages/{id}/parrots/{id}/foods
    
    @Transactional
    @GetMapping("/{cId}/parrots/{pId}/foods")
    public ResponseEntity<List<Food>> getFoodsByCageAndParrot(@PathVariable int cId, @PathVariable int pId) {
        // Usa il repository del pappagallo (assicurati di avere @Autowired ParrotRepository in CageController se non c'è, o sposta il controllo logico)
        Optional<Cage> optionalCage = cageRepository.findById(cId);
        if (optionalCage.isEmpty()) return ResponseEntity.notFound().build();
        
        Parrot foundParrot = null;
        for (Parrot p : optionalCage.get().getParrots()) {
            if (p.getId() == pId) foundParrot = p;
        }
        
        if (foundParrot == null) return ResponseEntity.notFound().build(); // Il pappagallo non è in questa gabbia
        
        List<Food> foods = new ArrayList<>(foundParrot.getFoods());
        return ResponseEntity.ok(foods);
    }

    
    
    // 10. METODO GERARCHICO (Hierarchical Endpoint) (TRIPLO) 
    // Restituisce tutti i pappagalli che sono in una determinata gabbia, solo se essi mangiano un determinato cibo e la determinata gabbia contiene il determinato cibo (come il metodo di Food getParrotsByFoodAndCage)
    // http://localhost:8080/api/cages/{id}/foods/{id}/parrots
    
    @Transactional
    @GetMapping("/{cId}/foods/{fId}/parrots")
    public ResponseEntity<List<Parrot>> getParrotsByCageAndFood(@PathVariable int cId, @PathVariable int fId) {
        Optional<Cage> optionalCage = cageRepository.findById(cId);
        if (optionalCage.isEmpty()) return ResponseEntity.notFound().build();
        
        // Verifica se la gabbia contiene quel cibo
        boolean cageHasFood = false;
        for (Food f : optionalCage.get().getFoods()) {
            if (f.getId() == fId) cageHasFood = true;
        }
        if (!cageHasFood) return ResponseEntity.notFound().build();
        
        // Trova i pappagalli in questa gabbia che lo mangiano
        List<Parrot> matchedParrots = new ArrayList<>();
        for (Parrot p : optionalCage.get().getParrots()) {
            for (Food f : p.getFoods()) {
                if (f.getId() == fId) {
                    matchedParrots.add(p);
                    break;
                }
            }
        }
        
        return ResponseEntity.ok(matchedParrots);
    }
}