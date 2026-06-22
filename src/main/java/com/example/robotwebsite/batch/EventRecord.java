package com.example.robotwebsite.batch;

import java.time.LocalDate;

public record EventRecord(
    Long siteSourceId,
    String title,
    LocalDate eventDate,
    String location,
    String url,
    String description,
    String genre,
    boolean targetBeginner,
    boolean targetKyuPlayer,
    boolean targetDanPlayer
) {}
