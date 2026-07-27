package com.example.robotwebsite.repository;

import com.example.robotwebsite.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByMatchDateBetweenOrderByMatchDateDesc(LocalDate startDate, LocalDate endDate);
    List<Match> findByMatchDateBetweenOrderByMatchDateAsc(LocalDate startDate, LocalDate endDate);
    Optional<Match> findByUrl(String url);
}
