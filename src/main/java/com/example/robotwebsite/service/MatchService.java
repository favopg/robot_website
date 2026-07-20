package com.example.robotwebsite.service;

import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);
    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrUpdate(Match match) {
        try {
            matchRepository.findByUrl(match.getUrl()).ifPresentOrElse(
                existing -> {
                    existing.setMatchDate(match.getMatchDate());
                    existing.setMatchName(match.getMatchName());
                    existing.setPlayer1Name(match.getPlayer1Name());
                    existing.setPlayer2Name(match.getPlayer2Name());
                    existing.setPlayer1Sente(match.getPlayer1Sente());
                    existing.setPlayer2Sente(match.getPlayer2Sente());
                    existing.setResult(match.getResult());
                    existing.setWinnerName(match.getWinnerName());
                    matchRepository.saveAndFlush(existing);
                },
                () -> matchRepository.saveAndFlush(match)
            );
        } catch (Exception e) {
            logger.error("Error saving match in new transaction: " + match.getUrl(), e);
            throw e; // Tasklet側でログを出すために再スロー
        }
    }
}
