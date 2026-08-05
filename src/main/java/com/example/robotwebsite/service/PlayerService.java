package com.example.robotwebsite.service;

import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Player> findByName(String name) {
        return playerRepository.findByName(name);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrUpdate(Player player) {
        try {
            // 名前で検索して存在すれば更新、なければ新規保存
            playerRepository.findByName(player.getName()).ifPresentOrElse(
                existing -> {
                    existing.setGender(player.getGender());
                    existing.setRank(player.getRank());
                    existing.setBirthPlace(player.getBirthPlace());
                    existing.setBirthDate(player.getBirthDate());
                    existing.setAffiliation(player.getAffiliation());
                    existing.setProfileUrl(player.getProfileUrl());
                    existing.setIconPath(player.getIconPath());
                    // likesCount はスクレイピングによる更新対象外とする（保持する）
                    playerRepository.saveAndFlush(existing);
                },
                () -> playerRepository.saveAndFlush(player)
            );
        } catch (Exception e) {
            logger.error("Error saving player in new transaction: " + player.getName(), e);
            throw e;
        }
    }

    @Transactional
    public int incrementLikes(String name) {
        return playerRepository.findByName(name).map(p -> {
            p.setLikesCount(p.getLikesCount() + 1);
            playerRepository.save(p);
            return p.getLikesCount();
        }).orElse(0);
    }

    @Transactional
    public int decrementLikes(String name) {
        return playerRepository.findByName(name).map(p -> {
            if (p.getLikesCount() > 0) {
                p.setLikesCount(p.getLikesCount() - 1);
                playerRepository.save(p);
            }
            return p.getLikesCount();
        }).orElse(0);
    }
}
