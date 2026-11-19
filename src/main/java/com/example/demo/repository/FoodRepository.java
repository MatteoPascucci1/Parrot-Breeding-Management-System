package com.example.demo.repository;

import com.example.demo.model.Food;
import com.example.demo.model.FoodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Integer> {
    
    // filtering by type (enum Foodtype) and pagination
    Page<Food> findByType(FoodType type, Pageable pageable);
    
    // filtering by name 
    Optional<Food> findByName(String name);
}