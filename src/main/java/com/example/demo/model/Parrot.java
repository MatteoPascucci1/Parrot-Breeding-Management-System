package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;





@Entity
@Table(name = "parrot")

public class Parrot {	

	
	
    @Id //primary key 
    @GeneratedValue(strategy = GenerationType.IDENTITY) //delegare la generazione della chiave primaria direttamente al database MySQL, sfruttando il meccanismo AUTO_INCREMENT (genera automaticamente l'ID successivo)
    private Integer id; // ID primario autoincrementante nel DB

    @NotBlank(message = "ring number is mandatory and cannot be left blank") 
    @Column(name = "ring_number", unique = true, nullable = false)
    private String ringNumber; //(es. "ITA-2024-9876")

    @NotBlank(message = "breed is mandatory")
    @Column(name = "breed", nullable = false)
    private String breed; //(es. "Ara Ararauna", "Cacatua")

    @NotBlank(message = "gender is mandatory")
    @Column(name = "gender", nullable = false)
    private String gender; //(es. "MALE", "FEMALE", "UNKNOWN")

    @NotNull(message = "age in months is mandatory")
    @Column(name = "age_months", nullable = false)
    private Integer ageMonths;

    @NotNull(message = "birth year is mandatory")
    @Column(name = "birth_year", nullable = false)
    private Integer birthYear; 

    
    
 // Relazione ManyToOne verso Cage (MOLTI pappagalli possono stare in UNA gabbia)
    @ManyToOne//(cascade = CascadeType.MERGE) //invece di provare a creare un nuovo Parrot da zero (o di schiantarsi perché mancano i campi obbligatori), Hibernate fa un merge: va a prendere i dati completi che già esistono nel database per quell'ID e li unisce alla transazione corrente, permettendo il salvataggio pulito dell'associazione.
    @JoinColumn(name = "cage_id") //cage_id è foreign key di Parrot, Parrot è lato attivo nella relazione join con Cage
    @JsonIgnoreProperties({"parrots", "foods"}) //*** per evitare loop a cascata nelle get
    private Cage cage;

 // Relazione ManyToMany verso Food (MOLTI pappagalli mangiano MOLTI tipi di cibo)
    @ManyToMany//(cascade = CascadeType.MERGE)//*****
    @JoinTable(
        name = "parrot_food",
        joinColumns = @JoinColumn(name = "parrot_id"),
        inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    @JsonIgnoreProperties({"parrots", "cages"}) //*** per evitare loop a cascata nelle get
    private List<Food> foods = new ArrayList<>();
    
    
    
    
    
    // Costruttore vuoto (richiesto obbligatoriamente da JPA)
    public Parrot() {
    }


    public Parrot(String ringNumber, String breed, String gender, Integer ageMonths, Integer birthYear) {
        this.ringNumber = ringNumber;
        this.breed = breed;
        this.gender = gender;
        this.ageMonths = ageMonths;
        this.birthYear = birthYear;
    }

    

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRingNumber() {
        return ringNumber;
    }

    public void setRingNumber(String ringNumber) {
        this.ringNumber = ringNumber;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAgeMonths() {
        return ageMonths;
    }

    public void setAgeMonths(Integer ageMonths) {
        this.ageMonths = ageMonths;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public Cage getCage() {
        return cage;
    }

    public void setCage(Cage cage) {
        this.cage = cage;
    }
    
    public List<Food> getFoods() {
        return foods;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }
}