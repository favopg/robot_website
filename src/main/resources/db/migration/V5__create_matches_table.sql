CREATE TABLE matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_source_id BIGINT,
    match_date DATE NOT NULL,
    match_name VARCHAR(255) NOT NULL,
    player1_name VARCHAR(100) NOT NULL,
    player2_name VARCHAR(100) NOT NULL,
    player1_sente BOOLEAN, -- TRUE if player1 is Sente (△)
    player2_sente BOOLEAN, -- TRUE if player2 is Sente (△)
    result VARCHAR(100),   -- e.g., '黒中押', '白半目'
    winner_name VARCHAR(100), -- Null for scheduled matches
    url VARCHAR(500) UNIQUE, -- Unique key to prevent duplicates
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (site_source_id) REFERENCES site_sources(id)
);

CREATE INDEX idx_matches_date ON matches(match_date);
CREATE INDEX idx_matches_winner ON matches(winner_name);
