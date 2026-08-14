package com.example.robotwebsite.repository;

import com.example.robotwebsite.entity.ReleaseInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseInfoRepository extends JpaRepository<ReleaseInfo, Long> {
}
