CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_source_id BIGINT,
    title VARCHAR(255) NOT NULL,
    event_date DATE,
    location VARCHAR(255),
    url VARCHAR(500) UNIQUE,
    description TEXT,
    genre VARCHAR(50) NOT NULL, -- 'IGO' or 'SHOGI'
    target_beginner BOOLEAN DEFAULT FALSE,
    target_kyu_player BOOLEAN DEFAULT FALSE,
    target_dan_player BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (site_source_id) REFERENCES site_sources(id)
);

-- 検索を高速化するためのインデックス
CREATE INDEX idx_events_genre ON events(genre);
CREATE INDEX idx_events_date ON events(event_date);
