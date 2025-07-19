# Домашнее задание №3 IT Лагеря Т1

**Цель**: Создать spring-boot starter, и проект byshop-prototype на его основе.

Стартер должен обеспечить:
* Модуль приема и исполнения команд.
* Валидацию команд в рамках указанных ограничений, в случае их
некорректности должна быть возвращена соответствующая ошибка.
* Выполняющиеся команды подразделяющиеся на две **ограниченные** очереди.
* Аннотацию @WeylandWatchingYou, которой отмечать методы подлежащие аудиту.
* Аудит в двух режимах: Kafka Topic, консоль.
* Публично доступные метрики: текущая занятость андроида, количество выполненных заданий для каждого автора.
* Централизованную обработку ошибок для Rest Api.

Проект byshop-prototype:
* Контроллер для приёма комманд.
* Демонстрация функционала стартера посредством этого контроллера.

## Запуск
Для запуска byshop-prototype через docker:
```sh
docker compose up prototype
```

Для запуска тестов через docker:
```sh
docker compose up test
```
После выполнения этой команды произойдёт сборка, тестирование и развёртывания приложения и Apache Kafka.

Для запуска в локальном окружении:
1. Добавляем starter в локальный репозиторий. (Выполнять внутри starter)
```sh
mvn install
```
2. Запуск Apache Kafka в контейнере, обязательно если не используется консольный аудит.
```
docker compose up -d broker
```
3. Запуск тестов. (Выполнять внутри byshop-prototype)
```sh
mvn test
```
4. Запуск приложения.
```sh
mvn spring-boot:run
```
## Описание реализации

Модуль приема и исполнения команд реализован с помощью двух ThreadPoolExecutor. Однопоточный без очереди для CRITICAL задач.
Один многопоточный, с ограниченной ёмкостью. При переполнении очередей, генерируется ошибка. При желании, клиент может 
заменить эти бины на другие, указав их в своей конфигурации. Для работы клиент должен получить Bean CommandDispatcher,
который будет управлять выполнением задач.

Сквозная функциональность (метрики и аудиты) реализованы при помощи аспектов. Аннотацией @WeylandWatchingYou помечаются методы,
подлежащие аудиту. Для сбора и публикации метрик используется spring-boot-actuator и micrometer.

Для демонстрации работы написаны системные тесты, проверяющие выполнение требований к системе.

***Внимание! Для выполнения большинства тестов требуется тестовый инстанс Apache Kafka. Как его запустить указано в руководстве по запуску.***

## Дополнительно

Запрос для отправки команд:

```sh
curl --header "Content-Type: application/json" --request POST --data "{\"description\":\"Test command\",\"priority\":\"COMMON\",\"author\":\"Me\",\"time\":\"1970-01-01T00:00:00Z\"}" http://localhost:8080/
```

Доступ к метрикам:
- Число выполняемых задач http://localhost:8080/actuator/metrics/executor.active?tag=name:commonExecutorMetric (commonExecutorMetric или criticalExecutorMetric для доступа к соответсвющему ExecutorService)
- Число выполненных задач http://localhost:8080/actuator/metrics/executor.completed?tag=name:commonExecutorMetric (commonExecutorMetric или criticalExecutorMetric для доступа к соответсвющему ExecutorService)
- Число задач в очереди на выполнение http://localhost:8080/actuator/metrics/executor.queued?tag=name:commonExecutorMetric (commonExecutorMetric или criticalExecutorMetric для доступа к соответсвющему ExecutorService)
- Число выполненных задач для каждого автора http://localhost:8080/actuator/metrics/synthetic.human_core_starter.commands.completed?tag=name:author (author - имя автора). Метрика появится после выполнения хотя-бы одной команды.

Доступ к сообщениям отправленным в Kafka:

```sh
docker exec -it broker /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic audit --from-beginning
```