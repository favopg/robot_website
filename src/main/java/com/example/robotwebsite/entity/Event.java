package com.example.robotwebsite.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_source_id")
    private Long siteSourceId;

    @Column(nullable = false)
    private String title;

    @Column(name = "event_date")
    private LocalDate eventDate;

    private String location;

    @Column(unique = true)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String genre;

    @Column(name = "target_beginner")
    private Boolean targetBeginner = false;

    @Column(name = "target_kyu_player")
    private Boolean targetKyuPlayer = false;

    @Column(name = "target_dan_player")
    private Boolean targetDanPlayer = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSiteSourceId() { return siteSourceId; }
    public void setSiteSourceId(Long siteSourceId) { this.siteSourceId = siteSourceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Boolean getTargetBeginner() { return targetBeginner; }
    public void setTargetBeginner(Boolean targetBeginner) { this.targetBeginner = targetBeginner; }

    public Boolean getTargetKyuPlayer() { return targetKyuPlayer; }
    public void setTargetKyuPlayer(Boolean targetKyuPlayer) { this.targetKyuPlayer = targetKyuPlayer; }

    public Boolean getTargetDanPlayer() { return targetDanPlayer; }
    public void setTargetDanPlayer(Boolean targetDanPlayer) { this.targetDanPlayer = targetDanPlayer; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
