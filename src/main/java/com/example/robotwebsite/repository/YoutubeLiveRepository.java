package com.example.robotwebsite.repository;

import com.example.robotwebsite.entity.YoutubeLive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YoutubeLiveRepository extends JpaRepository<YoutubeLive, Long> {
    Optional<YoutubeLive> findByLiveUrl(String liveUrl);
}
