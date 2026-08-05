package com.example.robotwebsite;

import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.repository.MatchRepository;
import com.example.robotwebsite.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("test")
public class DiagnosticPlayerTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    public void findMissingPlayers() throws Exception {
        List<Match> matches = matchRepository.findAll();
        Set<String> playerNamesInMatches = new HashSet<>();
        for (Match m : matches) {
            if (m.getPlayer1Name() != null) playerNamesInMatches.add(m.getPlayer1Name());
            if (m.getPlayer2Name() != null) playerNamesInMatches.add(m.getPlayer2Name());
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("missing_players.txt"), StandardCharsets.UTF_8)) {
            writer.write("Total unique players in matches: " + playerNamesInMatches.size() + "\n");

            List<Player> allPlayers = playerRepository.findAll();
            Set<String> playersWithProfiles = allPlayers.stream()
                    .filter(p -> p.getRank() != null && p.getIconPath() != null)
                    .map(Player::getName)
                    .collect(Collectors.toSet());

            Set<String> missingPlayers = new HashSet<>(playerNamesInMatches);
            missingPlayers.removeAll(playersWithProfiles);

            writer.write("--- Players missing full profiles (" + missingPlayers.size() + ") ---\n");
            for (String name : missingPlayers.stream().sorted().collect(Collectors.toList())) {
                Player p = playerRepository.findByName(name).orElse(null);
                if (p != null) {
                    writer.write("[INCOMPLETE] " + name + " (Rank: " + p.getRank() + ", Icon: " + p.getIconPath() + ")\n");
                } else {
                    writer.write("[MISSING]    " + name + "\n");
                }
            }
            writer.write("---------------------------------------\n");
        }
    }
}
