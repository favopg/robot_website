package com.example.robotwebsite.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_source_id")
    private Long siteSourceId;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    @Column(name = "match_name", nullable = false)
    private String matchName;

    @Column(name = "player1_name", nullable = false)
    private String player1Name;

    @Column(name = "player2_name", nullable = false)
    private String player2Name;

    @Column(name = "player1_sente")
    private Boolean player1Sente;

    @Column(name = "player2_sente")
    private Boolean player2Sente;

    private String result;

    @Column(name = "winner_name")
    private String winnerName;

    @Column(unique = true)
    private String url;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private String player1Icon;

    @Transient
    private String player2Icon;

    @Transient
    private LocalDate player1BirthDate;

    @Transient
    private LocalDate player2BirthDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSiteSourceId() { return siteSourceId; }
    public void setSiteSourceId(Long siteSourceId) { this.siteSourceId = siteSourceId; }

    public LocalDate getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }

    public String getMatchName() { return matchName; }
    public void setMatchName(String matchName) { this.matchName = matchName; }

    public String getPlayer1Name() { return player1Name; }
    public void setPlayer1Name(String player1Name) { this.player1Name = player1Name; }

    public String getPlayer2Name() { return player2Name; }
    public void setPlayer2Name(String player2Name) { this.player2Name = player2Name; }

    public Boolean getPlayer1Sente() { return player1Sente; }
    public void setPlayer1Sente(Boolean player1Sente) { this.player1Sente = player1Sente; }

    public Boolean getPlayer2Sente() { return player2Sente; }
    public void setPlayer2Sente(Boolean player2Sente) { this.player2Sente = player2Sente; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPlayer1Icon() { return player1Icon; }
    public void setPlayer1Icon(String player1Icon) { this.player1Icon = player1Icon; }

    public String getPlayer2Icon() { return player2Icon; }
    public void setPlayer2Icon(String player2Icon) { this.player2Icon = player2Icon; }

    public LocalDate getPlayer1BirthDate() { return player1BirthDate; }
    public void setPlayer1BirthDate(LocalDate player1BirthDate) { this.player1BirthDate = player1BirthDate; }

    public LocalDate getPlayer2BirthDate() { return player2BirthDate; }
    public void setPlayer2BirthDate(LocalDate player2BirthDate) { this.player2BirthDate = player2BirthDate; }
}
