-- 운영 DB에 수동 적용할 테이블 생성 SQL — 학습 퀴즈 '오늘 본 룰' 시청 기록
-- 이 SQL을 먼저 적용한 뒤 배포해야 한다(없으면 /api/quizzes/questions·viewed-rules가 500).
CREATE TABLE injecapstone.viewed_terms (
    id bigint NOT NULL AUTO_INCREMENT,
    created_at datetime(6) NOT NULL,
    game_id varchar(64) NOT NULL,
    term_id varchar(64) NOT NULL,
    viewed_date date NOT NULL,
    user_id bigint NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_viewed_terms_user_date_term (user_id, viewed_date, term_id),
    CONSTRAINT fk_viewed_terms_user
        FOREIGN KEY (user_id)
        REFERENCES injecapstone.users (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
