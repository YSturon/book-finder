📚 Book Finder
Парсер электронных библиотек
Кейс от Т-Банка на хакатоне Техконект.

🧩 Структура проекта
catalog-service/ — сервис работы с каталогами книг.
search-service/ — сервис поиска по библиотекам.
common/ — dto

⚙️ Стек технологий
Java
Spring Boot
Gradle
Jackson
Docker
PostgreSQL

🛠️ Запуск
(Из папки репозитория, все необходимые компоненты должны быть установлены).
1) docker-compose up --build - Запуск докера с бд.
2) .\gradlew clean build - билдим проект
3) .\gradlew.bat :catalog-service:bootRun - запуск микосервиса catalog
4) .\gradlew.bat :search-service:bootRun - запуск микросервиса search

POST запрос для парсинга книг и добавления в бд:

POST http://localhost:8080/api/v1/search
Content-Type: application/json
{
  "author": "",
  "title": ""
}

JSON с уже спаршенными книгами:
http://localhost:8081/api/v1/books
