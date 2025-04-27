# 📚 Book Finder

**Парсер электронных библиотек**  
Проект реализован в рамках кейса от **Т-Банка** на хакатоне **Техконект**.

---

## ⚙️ Стек технологий

- **Java 17**
- **Spring Boot 3**
- **Gradle 8**
- **PostgreSQL 16**
- **Docker / Docker Compose**
- **Jackson (работа с JSON)**

---

## 🧩 Архитектура проекта

- `catalog-service/` — микросервис работы с каталогами книг.
- `search-service/` — микросервис поиска и парсинга данных из библиотек.
- `common/` — общие компоненты (DTO, настройки).

База данных — PostgreSQL.

---

## 🛠️ Установка и запуск

> Все команды выполнять в папке репозитория.

### 1. Запуск базы данных PostgreSQL

```bash
docker-compose up -d
```
### 2. Сборка проекта

```bash
.\gradlew.bat clean build
```
### 3. Запуск микросервисов
```bash
# Запуск catalog-service
.\gradlew.bat :catalog-service:bootRun
```

```bash
# Запуск search-service
.\gradlew.bat :search-service:bootRun
```

## 🔗 API Эндпоинты

### POST запрос для парсинга книг:
```bash
POST http://localhost:8080/api/v1/search
Content-Type: application/json
{
  "author": "",
  "title": ""
}
```
### GET запрос для получения всех спарсенных книг:
```bash
GET http://localhost:8081/api/v1/books
```
