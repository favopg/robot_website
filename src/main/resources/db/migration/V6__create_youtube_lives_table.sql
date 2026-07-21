CREATE TABLE youtube_lives (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    live_url VARCHAR(1000) NOT NULL,
    scheduled_start_time TIMESTAMP,
    scheduled_start_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(live_url)
);
