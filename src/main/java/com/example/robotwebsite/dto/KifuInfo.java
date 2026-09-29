package com.example.robotwebsite.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class KifuInfo {
    private String dateStr;       // "20260920"
    private String displayDate;   // "2026年09月20日"
    private String matchName;     // 棋戦名
    private String blackPlayer;   // 黒番対局者
    private String whitePlayer;   // 白番対局者
    private String result;        // 結果（例: 黒中押し勝ち）

    public KifuInfo() {}

    public KifuInfo(String dateStr, String matchName, String blackPlayer, String whitePlayer, String result) {
        this.dateStr = dateStr;
        this.matchName = (matchName != null && !matchName.isEmpty()) ? matchName : "注目局";
        this.blackPlayer = (blackPlayer != null && !blackPlayer.isEmpty()) ? blackPlayer : "対局者(黒)";
        this.whitePlayer = (whitePlayer != null && !whitePlayer.isEmpty()) ? whitePlayer : "対局者(白)";
        this.result = result;
        
        if (dateStr != null && dateStr.length() == 8) {
            try {
                LocalDate d = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                this.displayDate = d.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
            } catch (Exception e) {
                this.displayDate = dateStr;
            }
        } else {
            this.displayDate = dateStr;
        }
    }

    public String getDateStr() { return dateStr; }
    public void setDateStr(String dateStr) { this.dateStr = dateStr; }

    public String getDisplayDate() { return displayDate; }
    public void setDisplayDate(String displayDate) { this.displayDate = displayDate; }

    public String getMatchName() { return matchName; }
    public void setMatchName(String matchName) { this.matchName = matchName; }

    public String getBlackPlayer() { return blackPlayer; }
    public void setBlackPlayer(String blackPlayer) { this.blackPlayer = blackPlayer; }

    public String getWhitePlayer() { return whitePlayer; }
    public void setWhitePlayer(String whitePlayer) { this.whitePlayer = whitePlayer; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}
