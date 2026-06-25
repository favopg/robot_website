package com.example.robotwebsite.repository;

import com.example.robotwebsite.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByMatchDateBetweenOrderByMatchDateAsc(LocalDate startDate, LocalDate endDate);
}
