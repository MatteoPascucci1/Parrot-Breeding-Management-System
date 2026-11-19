package com.example.demo.repository;

import com.example.demo.model.Cage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CageRepository extends JpaRepository<Cage, Integer> {
    
    // filtering by cageName
    Optional<Cage> findByCageName(String cageName);

    // filtering by cageName and pagination
    Page<Cage> findByCageNameContaining(String cageName, Pageable pageable); //il Containing fa si che il filtering applichi la ricerca a tutti i cageName che contengono la parola
}