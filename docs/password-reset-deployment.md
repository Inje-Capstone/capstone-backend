# 비밀번호 재설정 배포

백엔드 Docker Compose는 `.env.password-reset`이 있으면 읽습니다.
`APP_PASSWORDRESET_ENABLED=true`를 설정해야 재설정 API가 등록됩니다.

1. 운영 DB에 [토큰 테이블 생성 SQL](sql/20261121-create-password-reset-tokens.sql)을 적용합니다.
2. 서버의 `capstone-backend` 디렉터리에 `.env.password-reset`을 만들고
   읽기 권한을 제한합니다. 다음은 인증과 STARTTLS를 사용하는 SMTP 설정 예시입니다.

   ```dotenv
   APP_PASSWORDRESET_ENABLED=true
   APP_PASSWORDRESET_FRONTENDORIGIN=https://injecapstone.com
   APP_PASSWORDRESET_FROM=no-reply@example.test
   SPRING_MAIL_HOST=smtp.example.test
   SPRING_MAIL_PORT=587
   SPRING_MAIL_USERNAME=smtp-user
   SPRING_MAIL_PASSWORD=replace-with-server-secret
   SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
   SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
   SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED=true
   ```

   SMTP 값과 발신 주소는 실제 발송 서비스 설정에 맞춥니다.
   Compose가 값을 그대로 읽으므로 따옴표를 붙이지 않습니다.
   실제 설정 파일은 Git에 올리지 않습니다.
3. 백엔드 컨테이너를 재생성하고 Swagger에서 `request`와 `confirm` 경로를 확인합니다.
   테스트 계정으로 요청 접수(`202`), 메일 수신, 비밀번호 변경(`204`),
   링크 재사용 거절(`400`)까지 확인합니다.