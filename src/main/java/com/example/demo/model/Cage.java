package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 




@Entity
@Table(name = "cage")

public class Cage {

	
	
    @Id //primary key 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID identificativo univoco autoincrementante della gabbia nel DB

    @NotBlank(message = "cage name is mandatory")
    @Column(name = "cage_name", unique = true, nullable = false)
    private String cageName; //(es. "GABBIA-01", "A12")

    @NotNull(message = "max capacity is mandatory")
    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity; //max number of parrots in a cage

    
    
 // Relazione OneToMany verso Parrot (UNA gabbia contiene MOLTI pappagalli)
 // mappedBy fa riferimento al campo "cage" presente nella classe Parrot
    @OneToMany(mappedBy = "cage") //cage (cage_id) è foreign key in Parrot, Cage è lato passivo nella relazione join con Parrot
    @JsonIgnoreProperties({"cage", "foods"}) //*** per evitare loop a cascata nelle get
    private List<Parrot> parrots = new ArrayList<>();

    	
 // Relazione ManyToMany verso Food (UNA gabbia contiene MOLTI tipi di cibo)
    @ManyToMany//(cascade = CascadeType.MERGE) //*****
    @JoinTable(
        name = "cage_food",
        joinColumns = @JoinColumn(name = "cage_id"),
        inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    @JsonIgnoreProperties({"parrots", "cages"}) //*** per evitare loop a cascata nelle get
    private List<Food> foods = new ArrayList<>();

   
    
    
    
    // Costruttore vuoto (richiesto da JPA)
    public Cage() {
    }


    public Cage(String cageName, Integer maxCapacity) {
        this.cageName = cageName;
        this.maxCapacity = maxCapacity;
    }

    // --- METODO DI UTILITÀ PER CALCOLARE I PAPPAGALLI PRESENTI ---
    // Non salviamo questo dato nel DB come colonna fissa per evitare ridondanze:
    // il numero di pappagalli attuali è semplicemente la dimensione della lista!
    public Integer getCurrentParrotCount() {
    	if (parrots != null) {
            return parrots.size();
        } else {
            return 0;
        }    
    }


    
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCageName() {
        return cageName;
    }

    public void setCageName(String cageName) {
        this.cageName = cageName;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public List<Parrot> getParrots() {
        return parrots;
    }

    public void setParrots(List<Parrot> parrots) {
        this.parrots = parrots;
    }
    
    public List<Food> getFoods() {
        return foods;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }
}