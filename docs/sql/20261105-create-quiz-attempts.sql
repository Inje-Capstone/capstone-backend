-- 운영 DB에 수동 적용한 테이블 생성 SQL
CREATE TABLE injecapstone.quiz_attempts (
    id bigint NOT NULL AUTO_INCREMENT,
    correct bit(1) NOT NULL,
    question_id int NOT NULL,
    selected_option_id int NOT NULL,
    submitted_at datetime(6) NOT NULL,
    user_id bigint NOT NULL,
    PRIMARY KEY (id),
    KEY fk_quiz_attempts_user (user_id),
    CONSTRAINT fk_quiz_attempts_user
        FOREIGN KEY (user_id)
        REFERENCES injecapstone.users (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
