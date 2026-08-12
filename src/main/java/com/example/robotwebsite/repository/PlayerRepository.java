package com.example.robotwebsite.repository;

import com.example.robotwebsite.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByName(String name);

    // いいね数が5以上かつ多い順に上位N名を取得する
    List<Player> findTop20ByLikesCountGreaterThanEqualOrderByLikesCountDesc(int minLikes);
}
