-- 비밀번호 재설정 기능을 켜기 전에 운영 DB에 수동 적용합니다.
CREATE TABLE IF NOT EXISTS injecapstone.password_reset_tokens (
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    issued_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_password_reset_tokens_hash (token_hash),
    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES injecapstone.users (id)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;