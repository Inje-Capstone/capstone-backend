-- 포인트 잔액 조회 API를 배포하기 전에 운영 DB에 수동 적용합니다.
CREATE TABLE IF NOT EXISTS injecapstone.point_transactions (
                                                               id BIGINT NOT NULL AUTO_INCREMENT,
                                                               user_id BIGINT NOT NULL,
                                                               amount INT NOT NULL,
                                                               event_key VARCHAR(191) NOT NULL,
    recorded_at_utc DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_point_transactions_user_event (user_id, event_key),
    CONSTRAINT fk_point_transactions_user
    FOREIGN KEY (user_id)
    REFERENCES injecapstone.users (id)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci;