package com.example.demo.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 




@Entity
@Table(name = "food")



public class Food {
	
	
	
    @Id //primary key 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "name of food is mandatory")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // Con EnumType.STRING salvi nel DB il testo esatto dell'enum (es. "FRUTTA_SECCA")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "type of food is mandatory")
    @Column(name = "type", nullable = false)
    private FoodType type;

    
 
    // Relazione @ManyToMany verso Food (MOLTI cibi sono contenuti)
    @ManyToMany(mappedBy = "foods") //Food è lato passivo nella relazione join table con Cage
    @JsonIgnoreProperties({"cage", "foods"}) //*** per evitare loop a cascata nelle get
    private List<Parrot> parrots = new ArrayList<>();

 // Relazione ManyToMany inversa verso Cage
    @ManyToMany(mappedBy = "foods") //Food è lato passivo nella relazione join table con Parrot
    @JsonIgnoreProperties({"parrots", "foods"}) //*** per evitare loop a cascata nelle get
    private List<Cage> cages = new ArrayList<>();

    
    
    public Food() {
    }

    public Food(String name, FoodType type) {
        this.name = name;
        this.type = type;
    }

    
    


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FoodType getType() {
        return type;
    }

    public void setType(FoodType type) {
        this.type = type;
    }

    public List<Parrot> getParrots() {
        return parrots;
    }

    public void setParrots(List<Parrot> parrots) {
        this.parrots = parrots;
    }
    
    public List<Cage> getCages() {
        return cages;
    }

    public void setCages(List<Cage> cages) {
        this.cages = cages;
    }
}