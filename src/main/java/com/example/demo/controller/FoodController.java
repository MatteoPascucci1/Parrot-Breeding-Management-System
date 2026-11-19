package com.example.demo.controller;

import com.example.demo.model.Cage;
import com.example.demo.model.Food;
import com.example.demo.model.FoodType;
import com.example.demo.model.Parrot;
import com.example.demo.repository.FoodRepository;

import jakarta.transaction.Transactional;
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



@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/foods")

public class FoodController {

    @Autowired
    private FoodRepository foodRepository;

    
    
 
    // 1. CREATE (POST) (insert)
	// http://localhost:8080/api/foods		+ body JSON

    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMINISTRATOR')")
    public Food createFood(@Valid @RequestBody Food food) {
        return foodRepository.save(food);
    }
    
    
    
    
    
    // 2. READ (GET) (select)
    // http://localhost:8080/api/foods/{id}

    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable int id) {
        Optional<Food> food = foodRepository.findById(id);
        return food.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    
    
    
    
    // 3.  READ (GET all) (select) (lista con Paginazione e Filtraggio)     
    // http://localhost:8080/api/foods (all) + ?page=0&size=5&type=FRUIT (per pagination e filtering)

    @GetMapping
    public Page<Food> getAllFoods(
            @RequestParam(required = false) FoodType type,
            Pageable pageable) {
        
        if (type != null) {
            return foodRepository.findByType(type, pageable);
        } else {
            return foodRepository.findAll(pageable);
        }
    }

  
    
    

    // 4. UPDATE (PUT) (update)
    // http://localhost:8080/api/foods/{id}		+ body JSON

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMINISTRATOR')")
    public ResponseEntity<Food> updateFood(@PathVariable int id, @Valid @RequestBody Food foodDetails) {
        Optional<Food> optionalFood = foodRepository.findById(id);
        
        if (optionalFood.isPresent()) {
            Food food = optionalFood.get();
            food.setName(foodDetails.getName());
            food.setType(foodDetails.getType());
            
            Food updatedFood = foodRepository.save(food);
            return ResponseEntity.ok(updatedFood);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    
    
    
    // 5. UPDATE (PATCH) (update)
    // http://localhost:8080/api/foods/{id}		+ body JSON
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMINISTRATOR')")
    public ResponseEntity<Food> patchFood(@PathVariable int id, @RequestBody Food foodUpdates) {
        Optional<Food> optionalFood = foodRepository.findById(id);
        
        if (optionalFood.isPresent()) {
            Food food = optionalFood.get();
            
            // Aggiorna solo i campi effettivamente presenti nel JSON
            if (foodUpdates.getName() != null) food.setName(foodUpdates.getName());
            if (foodUpdates.getType() != null) food.setType(foodUpdates.getType());
            
            Food updatedFood = foodRepository.save(food);
            return ResponseEntity.ok(updatedFood);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    
    

    // 6. DELETE (DELETE) (delete)
    // http://localhost:8080/api/foods/{id}

    @Transactional
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteFood(@PathVariable int id) {
        Optional<Food> optionalFood = foodRepository.findById(id);

        if (optionalFood.isPresent()) {
            Food food = optionalFood.get();

            //rimuove questo food da tutti i pappagalli che lo mangiano
            for (Parrot parrot : food.getParrots()) {
                parrot.getFoods().remove(food);
            }

            //rimuove questo food da tutte le gabbie che lo contengono
            for (Cage cage : food.getCages()) {
                cage.getFoods().remove(food);
            }

            //ora che tutte tabelle join liberate dal food, posso eliminarlo
            foodRepository.delete(food);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    
    
    
    
    
    
    
    // 7. METODO GERARCHICO (Hierarchical Endpoint) (DOPPIO)
    // Restituisce tutti i pappagalli che mangiano un determinato cibo
    // http://localhost:8080/api/foods/{id}/parrots
    
    @Transactional
    @GetMapping("/{foodId}/parrots")
    public ResponseEntity<List<Parrot>> getParrotsByFood(@PathVariable int foodId) {
        Optional<Food> optionalFood = foodRepository.findById(foodId);

        if (optionalFood.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Forza il caricamento della lista in una nuova ArrayList per aggirare il Lazy Loading
        List<Parrot> pappagalliAssociati = new ArrayList<>(optionalFood.get().getParrots());

        return ResponseEntity.ok(pappagalliAssociati);
    }
    
    
    
    // 8. METODO GERARCHICO (Hierarchical Endpoint) (DOPPIO)
    // Restituisce tutte le gabbie che contengono un determinato cibo
    // http://localhost:8080/api/foods/{id}/cages    
    
    @Transactional
    @GetMapping("/{foodId}/cages")
    public ResponseEntity<List<Cage>> getCagesByFood(@PathVariable int foodId) {
        Optional<Food> optionalFood = foodRepository.findById(foodId);

        if (optionalFood.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Bypassa il Lazy Loading caricando i dati in una lista Java standard
        List<Cage> gabbieRifornite = new ArrayList<>(optionalFood.get().getCages());

        return ResponseEntity.ok(gabbieRifornite);
    }
    
    
    
    // 9. METODO GERARCHICO (Hierarchical Endpoint) (TRIPLO)    
    // Restituisce la gabbia di un determinato pappagallo, solo se esso mangia un determinato cibo
    // http://localhost:8080/api/foods/{id}/parrots/{id}/cage  
    
    @Transactional
    @GetMapping("/{fId}/parrots/{pId}/cage")
    public ResponseEntity<Cage> getCageByFoodAndParrot(@PathVariable int fId, @PathVariable int pId) {
        Optional<Food> optionalFood = foodRepository.findById(fId);
        if (optionalFood.isEmpty()) return ResponseEntity.notFound().build();
        
        Parrot foundParrot = null;
        for (Parrot p : optionalFood.get().getParrots()) {
            if (p.getId() == pId) foundParrot = p;
        }
        
        if (foundParrot == null || foundParrot.getCage() == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(foundParrot.getCage());
    }
    
    

    // 10. METODO GERARCHICO (Hierarchical Endpoint) (TRIPLO) 
    // Restituisce tutti i pappagalli che mangiano un determinato cibo, solo se essi sono in una determinata gabbia e la determinata gabbia contiene il determinato cibo (come metodo di Cage getParrotsByCageAndFood) 
    // http://localhost:8080/api/foods/{id}/cages/{id}/parrots
    
    @Transactional
    @GetMapping("/{fId}/cages/{cId}/parrots")
    public ResponseEntity<List<Parrot>> getParrotsByFoodAndCage(@PathVariable int fId, @PathVariable int cId) {
        Optional<Food> optionalFood = foodRepository.findById(fId);
        if (optionalFood.isEmpty()) return ResponseEntity.notFound().build();
        
        // Controlla che il cibo sia effettivamente somministrato in quella gabbia
        boolean foodInCage = false;
        for (Cage c : optionalFood.get().getCages()) {
            if (c.getId() == cId) foodInCage = true;
        }
        if (!foodInCage) return ResponseEntity.notFound().build();
        
        // Estrai i pappagalli associati a questo cibo che vivono in quella specifica gabbia
        List<Parrot> matchedParrots = new ArrayList<>();
        for (Parrot p : optionalFood.get().getParrots()) {
            if (p.getCage() != null && p.getCage().getId() == cId) {
                matchedParrots.add(p);
            }
        }
        
        return ResponseEntity.ok(matchedParrots);
    }
    
}