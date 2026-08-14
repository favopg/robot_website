package com.example.robotwebsite.repository;

import com.example.robotwebsite.entity.ReleaseInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReleaseInfoRepository extends JpaRepository<ReleaseInfo, Long> {
    List<ReleaseInfo> findTop5ByOrderByCreatedAtDesc();
    List<ReleaseInfo> findAllByOrderByCreatedAtDesc();
}
