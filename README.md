# Subscription Service

Сервис учёта пользовательских подписок на различные сервисы и тарифы. Реализован на **Kotlin** с использованием **Spring Boot 3**, предоставляет REST API для полного управления жизненным циклом подписок, поддерживает фильтрацию, пагинацию, аудит изменений и автоматический мониторинг.

##  Реализованный функционал

###  Основное API
| Метод | Endpoint | Описание |
|-------|----------|----------|
| `POST`   | `/api/subscriptions` | Создание новой подписки |
| `GET`    | `/api/subscriptions/{id}` | Получение подписки по ID |
| `GET`    | `/api/subscriptions` | Получение списка с фильтрами и пагинацией |
| `PATCH`  | `/api/subscriptions/{id}/status` | Изменение статуса подписки |
| `DELETE` | `/api/subscriptions/{id}/cancel` | Отмена подписки |
| `PATCH`  | `/api/subscriptions/{id}/suspend` | Приостановка подписки |
| `GET`    | `/api/subscriptions/user/{userId}/active` | Активные подписки пользователя |
| `GET`    | `/api/subscriptions/{id}/history` | История изменений статуса |

###  Функциональные требования
-  **Корректный жизненный цикл**: `ACTIVE` → `SUSPENDED` → `CANCELLED` → `EXPIRED`
-  **Бизнес-валидация**: запрет активации истёкшей подписки без продления срока действия
-  **Фильтрация**: по пользователю, названию сервиса, статусу, диапазону дат начала/окончания
-  **Пагинация и сортировка**: поддержка `page`, `size`, `sortBy`, `sortDirection`
-  **Валидация входных данных**: проверка корректности дат и положительной стоимости (`@AssertTrue`, `@Positive`)

###  Дополнительно реализовано
-  **Scheduler**: автоматический перевод подписок в статус `EXPIRED` по расписанию (каждые 60 сек)
-  **История статусов**: таблица `subscription_history` с фиксацией `oldStatus` → `newStatus` и таймстампа
-  **Spring Boot Actuator**: метрики, health-чеки, Prometheus-формат
-  **Docker Compose**: полная контейнеризация приложения + PostgreSQL
-  **Тестовое покрытие**: Unit-тесты сервиса, интеграционные тесты контроллера и репозиториев (H2)

##  Стек технологий

| Категория | Технологии |
|-----------|------------|
| Язык | Kotlin 1.9+ |
| Фреймворк | Spring Boot 3.2.x |
| ORM | Spring Data JPA + Hibernate 6 |
| БД | PostgreSQL 16 (H2 для тестов) |
| Миграции | Liquibase |
| Документация API | OpenAPI 3 / Swagger UI |
| Тестирование | JUnit 5, Mockito, Spring Test |
| Мониторинг | Actuator, Micrometer Prometheus |
| Контейнеризация | Docker, Docker Compose |

## Быстрый старт

### Локальный запуск
```bash
# 1. Сборка проекта
mvn clean package -DskipTests

# 2. Запуск приложения
java -jar target/subscription-service-0.0.1-SNAPSHOT.jar
# или через Maven Wrapper
./mvnw spring-boot:run
```
Приложение доступно на: `http://localhost:8080`

### Запуск через Docker Compose
```bash
docker-compose up -d --build

# Просмотр логов
docker-compose logs -f subscription-service
```
Контейнеры запускают:
- PostgreSQL на порту `5433`
Приложение на порту `8080`

##  API и Документация

Интерактивная документация доступна после запуска:
👉 **Swagger UI**: http://localhost:8080/swagger-ui.html

Пример запроса на создание подписки:
```bash
curl -X POST http://localhost:8080/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "serviceName": "YouTube Premium",
    "startDate": "2024-01-01T10:00:00",
    "endDate": "2025-01-01T10:00:00",
    "cost": 11.99,
    "autoRenew": true
  }'
```

## 📊 Мониторинг (Actuator)

| Endpoint | Описание |
|----------|----------|
| `/actuator/health` | Статус приложения и БД |
| `/actuator/info` | Информация о версии и сборке |
| `/actuator/metrics` | Все доступные метрики |
| `/actuator/env` | Активные переменные окружения |

Пример проверки здоровья:
```bash
curl http://localhost:8080/actuator/health
```

## 🧪 Тестирование

Запуск всех тестов:
```bash
mvn test
```

Тесты покрывают:
-  `SubscriptionServiceTest` — бизнес-логика и валидация
-  `SubscriptionControllerTest` — HTTP-слой и сериализация
- `SubscriptionRepositoryTest` — работа с БД (H2)

