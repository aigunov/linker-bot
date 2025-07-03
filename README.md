

---


# 🛠 Scrapper: Notification & Tracking Service

## 🚀 О проекте

**Scrapper** — это микросервис для:
- отслеживания ссылок (GitHub, StackOverflow)
- фильтрации по тегам и атрибутам
- уведомлений пользователей Telegram через HTTP или Kafka
- с fallback в Kafka, если HTTP недоступен
- поддержки Digest-рассылок
- с Rate Limiting и Resilience4j (Retry, CircuitBreaker, TimeLimiter)

Создан в качестве **pet-проекта**, чтобы отработать:
- проектирование слоистой архитектуры
- интеграцию с PostgreSQL, Redis и Kafka
- паттерны отказоустойчивости
- тесты с Testcontainers

---

## 🧭 Архитектура

```plaintext
    +--------------------+
    |    Telegram Bot     |
    +---------+----------+
              |
    HTTP / Kafka (fallback)
              |
       +------+------+
       |   Scrapper   |
       +------+------+
              |
   +----------+----------+
   |    PostgreSQL DB    |
   +---------------------+
   |     Redis Cache     |
   +---------------------+
   | External APIs (GitHub, SO)
   +---------------------+
````

---

## ⚙️ Стек технологий

| Уровень     | Технологии                                    |
| ----------- | --------------------------------------------- |
| Язык        | Java 21                                       |
| Framework   | Spring Boot 3, Spring Data, Spring Retry      |
| Data Store  | PostgreSQL, Redis                             |
| Messaging   | Apache Kafka                                  |
| HTTP Client | Spring WebClient, RestClient                  |
| Resilience  | Resilience4j (Retry, CircuitBreaker, Timeout) |
| Миграции    | Liquibase                                     |
| Тесты       | JUnit 5, Testcontainers, WireMock, Mockito    |
| Логирование | SLF4J + Logback                               |

---

## ✨ Возможности

✅ Отслеживание ссылок по Tg-чатам

✅ Рассылка уведомлений через HTTP или Kafka (fallback) 

✅ Digest — периодические сборки обновлений 

✅ Поддержка тегов и фильтров 

✅ Rate Limiting с Redis 

✅ Circuit Breaker + Retry для GitHub/SO и Bot 

✅ Полные интеграционные тесты (Testcontainers)

---

## 🔥 Технические детали

### 🚨 Fallback HTTP ➔ Kafka

Если HTTP к Bot недоступен — сразу fallback:

```java
try {
    webClient.post().uri("/updates").bodyValue(update).retrieve().toBodilessEntity();
} catch (Exception e) {
    log.warn("Fallback to Kafka");
    kafkaTemplate.send(notificationTopic, update);
}
```

### ⚡ Circuit Breaker

```yaml
client:
  resilience:
    bot-client:
      timeout: 2s
      max-attempts: 3
      wait-duration: 1s
      retry-statuses: [502, 503, 504, 500, 429]
```

---

## 🚀 Быстрый старт

### 🐳 С Docker

```bash
docker-compose up -d
./gradlew bootRun
```

> `docker-compose.yml`

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:17.4
    ports:
      - "6432:5432"
    environment:
      POSTGRES_DB: scrapper_db
      POSTGRES_USER: aigunov
      POSTGRES_PASSWORD: 12345

  redis:
    image: redis:7
    ports:
      - "6379:6379"
```

---

### 💻 Локально без Docker

> PostgreSQL на `localhost:6432`, Redis на `localhost:6379` по умолчанию
в ином случае через application.yaml


## 🔗 Примеры API

### POST `/tg-chat/123`

Создание чата.

```http
POST /tg-chat/123
{
  "name": "my-chat"
}
```

---

### POST `/links`

Добавление ссылки.

```http
POST /links
{
  "uri": "https://github.com/user/repo",
  "tags": ["java", "spring"],
  "filters": ["label:bug"]
}
```

---

## ⚙️ Настройка

Все параметры в `application.yml`:

```yaml
app:
  message:
    transport: HTTP
    kafka:
      topic:
        notification: test-notification
        digest: test-digest
  db:
    access-type: sql
client:
  resilience:
    bot-client:
      timeout: 2s
      max-attempts: 3
      wait-duration: 1s
```


---



