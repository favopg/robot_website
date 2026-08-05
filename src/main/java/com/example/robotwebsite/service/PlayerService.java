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

    /**
     * 名前から称号や段位を取り除いて正規化する
     * 例: "一力遼 名人" -> "一力遼", "芝野虎丸 棋聖" -> "芝野虎丸", "仲邑菫 三段" -> "仲邑菫"
     */
    public String normalizeName(String name) {
        if (name == null) return null;

        String input = name.trim();

        // 特殊な称号・別名の置換（称号を含めた完全一致で判定）
        if (input.equals("二十四世本因坊秀芳")) {
            return "石田芳夫";
        }

        // 段位や称号（名人、棋聖、本因坊など）を末尾から取り除く
        String normalized = input.replaceAll("[\\s\u3000]*(([初一二三四五六七八九十]|\\d+)段|名人|本因坊|棋聖|碁聖|十段|天元|王座|女流[^\u3000\\s]+|扇興杯).*$", "").trim();

        // 異体字や別名の補正
        // 柳原咲輝 -> 栁原咲輝
        if ("柳原咲輝".equals(normalized)) {
            normalized = "栁原咲輝";
        }

        return normalized;
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
