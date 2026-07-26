package com.example.robotwebsite.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String gender;
    private String rank;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_place")
    private String birthPlace;

    private String master;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "icon_path")
    private String iconPath;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }

    public String getMaster() { return master; }
    public void setMaster(String master) { this.master = master; }

    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }

    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
