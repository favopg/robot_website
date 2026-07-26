CREATE TABLE players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE, -- 棋士名（検索キー）
    gender VARCHAR(10),                -- 性別
    rank VARCHAR(20),                  -- 段位
    birth_date DATE,                   -- 生年月日
    birth_place VARCHAR(200),          -- 出身地
    master VARCHAR(100),               -- 門下
    profile_url VARCHAR(500),          -- 日本棋院のプロフィールURL
    icon_path VARCHAR(500),            -- アイコン画像のパス
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_players_name ON players(name);
