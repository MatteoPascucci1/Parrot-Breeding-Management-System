package com.example.demo.repository;

import com.example.demo.model.Parrot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParrotRepository extends JpaRepository<Parrot, Integer> {
	
	// JpaRepository<Parrot, Integer> prende in input:
    // 1. L'entità da gestire (Parrot)
    // 2. Il tipo di dato della chiave primaria (@Id), che nel nostro caso è int (o Integer)
    
    // methods for filtering and pagination
    Page<Parrot> findByBreed(String breed, Pageable pageable);
    Page<Parrot> findByGender(String gender, Pageable pageable);
    Page<Parrot> findByBreedAndGender(String breed, String gender, Pageable pageable);
    Page<Parrot> findByAgeMonths(Integer ageMonths, Pageable pageable);
    Page<Parrot> findByBirthYear(Integer birthYear, Pageable pageable);
}