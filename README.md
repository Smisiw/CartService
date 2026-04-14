# CartService

Микросервис корзины покупателя. Хранит товарные позиции в PostgreSQL, кэширует данные в Redis, валидирует состав корзины через Feign-вызов к ProductService. При получении Kafka-события `order.created` автоматически очищает корзину пользователя после оформления заказа.

- **Порт:** 8083
- **Имя в Eureka:** `CART-SERVICE`
- **Java:** 21
- **Spring Boot:** 3.4.4

---

## Содержание

- [Назначение](#назначение)
- [API эндпоинты](#api-эндпоинты)
- [Модель данных](#модель-данных)
- [Kafka-интеграция](#kafka-интеграция)
- [Feign-клиент (исходящий)](#feign-клиент-исходящий)
- [Переменные окружения](#переменные-окружения)
- [Сборка и запуск](#сборка-и-запуск)
- [Тесты](#тесты)

---

## Назначение

- Хранит корзину каждого аутентифицированного пользователя в PostgreSQL (ключ — UUID пользователя из JWT).
- Кэширует результаты в Redis.
- Поддерживает слияние анонимной (гостевой) корзины с корзиной авторизованного пользователя (`/api/cart/merge`).
- Валидирует вариации товаров через Feign-вызов к ProductService перед возвратом данных клиенту.
- Слушает Kafka-топик `order.created` и очищает позиции заказа из корзины после его оформления.

---

## API эндпоинты

Доступны через Gateway: `http://localhost:8080`. Пометка `[AUTH]` — требуется JWT-токен.

| Метод | Путь                | Auth   | Описание                                                          |
|-------|---------------------|--------|-------------------------------------------------------------------|
| GET   | `/api/cart`         | [AUTH] | Корзина текущего пользователя с актуальными данными о товарах    |
| POST  | `/api/cart/add`     | [AUTH] | Добавить одну единицу вариации товара в корзину                  |
| POST  | `/api/cart/subtract`| [AUTH] | Уменьшить количество позиции на единицу                          |
| POST  | `/api/cart/clear`   | [AUTH] | Очистить всю корзину текущего пользователя                       |
| POST  | `/api/cart/merge`   | [AUTH] | Слить переданную (гостевую) корзину с корзиной пользователя      |
| POST  | `/api/cart/validate`| —      | Валидировать состав корзины без аутентификации                   |

### Тело запросов

**`POST /api/cart/add`** и **`POST /api/cart/subtract`:**
```json
{"productVariationId": "uuid-вариации-товара"}
```

**`POST /api/cart/merge`** и **`POST /api/cart/validate`:**
```json
{
  "items": [
    {"productVariationId": "uuid", "quantity": 2}
  ]
}
```

---

## Модель данных

| Сущность   | Поля                                       | Описание                                 |
|------------|--------------------------------------------|------------------------------------------|
| `Cart`     | `id` (UUID), `userId` (UUID)               | Корзина пользователя; `userId` из JWT    |
| `CartItem` | `id`, `productVariationId` (UUID), `quantity`, `cart` | Позиция корзины           |

Схема управляется Flyway (`classpath:db/migration`).

---

## Kafka-интеграция

**Консьюмер:** топик `order.created`, группа `cart-service-group`

При получении события `OrderCreatedEvent` извлекает список `productVariationId` из позиций заказа и удаляет соответствующие позиции из корзины пользователя.

---

## Feign-клиент (исходящий)

CartService вызывает ProductService для получения актуальных данных о вариациях товаров.

| Интерфейс              | Целевой сервис    | Метод | Эндпоинт                         |
|------------------------|-------------------|-------|----------------------------------|
| `ProductServiceClient` | `PRODUCT-SERVICE` | POST  | `/api/products/variationsByIds`  |

---

## Переменные окружения

| Переменная                | По умолчанию (dev)                                 | Описание                  |
|---------------------------|----------------------------------------------------|---------------------------|
| `DB_URL`                  | `jdbc:postgresql://localhost:5435/cart_db`         | JDBC-URL PostgreSQL       |
| `DB_USERNAME`             | `user`                                             | Пользователь БД           |
| `DB_PASSWORD`             | `password`                                         | Пароль БД                 |
| `JWT_SECRET`              | `nTDmGYqtvLfDCptgzwG+xKGtXV/JHL4fHKJrxK9tHdI=`   | Ключ проверки JWT         |
| `KAFKA_BOOTSTRAP_SERVERS` | `http://localhost:9092`                            | Адрес Kafka-брокера       |
| `REDIS_HOST`              | `localhost`                                        | Хост Redis                |
| `REDIS_PORT`              | `6379`                                             | Порт Redis                |
| `EUREKA_URL`              | `http://localhost:8761/eureka/`                    | Адрес Eureka Discovery    |

---

## Сборка и запуск

### Через Docker Compose

```bash
cd MarketPlaceProject
docker compose -f docker-compose.dev.yml up --build cart-service cart-db redis -d
```

### Локально из исходников

Требования: JDK 21, PostgreSQL 15 (база `cart_db`), Redis, Kafka, запущенный ProductService.

```bash
cd CartService
KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
REDIS_HOST=localhost \
./gradlew bootRun
```

### Сборка JAR

```bash
cd CartService
./gradlew build
```

---

## Тесты

```bash
cd CartService
./gradlew test
```

Тестовые классы в `src/test/java/ru/projects/cart_service/`:
- `service/CartServiceTest.java`

Health-check endpoint: `GET http://localhost:8083/actuator/health`
