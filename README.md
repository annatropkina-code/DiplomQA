# Автоматизация тестирования покупки тура

Проект содержит UI-тесты оплаты тура и оформления кредита, а также SQL-проверки сохранённых результатов.

План автоматизации находится в Plan.md.

## Требования

На компьютере с Windows:

- Git;
- JDK 11;
- IntelliJ IDEA;
- Google Chrome;
- SSH-клиент для подключения к ВМ, например PuTTY.

В IDEA для *Project SDK* и *Gradle JVM* необходимо выбрать JDK 11. Используется Gradle Wrapper 8.8, отдельная установка Gradle не требуется.

На виртуальной машине Linux должны быть установлены Git, Docker и Docker Compose. В инструкции используется команда docker-compose.

База данных и эмулятор банка запускаются на ВМ. Приложение и тесты запускаются на компьютере. Node.js отдельно на компьютер устанавливать не требуется.

Для загрузки проекта, зависимостей и Docker-образов необходимо подключение к интернету.

## Получение проекта

На компьютере выполнить команды по очереди:
git clone https://github.com/annatropkina-code/DiplomQA.git

cd DiplomQA

Открыть проект в IDEA и дождаться загрузки зависимостей Gradle.

Подключиться к ВМ по SSH. На ВМ также получить проект:
git clone https://github.com/annatropkina-code/DiplomQA.git

cd DiplomQA

## Запуск с MySQL

В папке проекта на ВМ выполнить: docker-compose up -d mysqldb gate-simulator

Проверить состояние и журналы:
docker-compose ps
docker logs --tail=30 diplomqa_mysqldb_1
docker logs --tail=30 diplomqa_gate-simulator_1

Дождаться готовности MySQL: сообщения "ready for connections" на порту 3306. Эмулятор при первом запуске устанавливает зависимости, затем выводит список тестовых карт.

Оба контейнера должны оставаться работающими. Порты ВМ 3306 и 9999 должны быть доступны с компьютера.

В корневом application.properties **на компьютере** указать:

spring.credit-gate.url=http://VM_IP:9999/credit
spring.payment-gate.url=http://VM_IP:9999/payment
spring.datasource.url=jdbc:mysql://VM_IP:3306/app
spring.datasource.username=app
spring.datasource.password=pass

**Вместо VM_IP подставить IP своей виртуальной машины.**

Сохранить файл.

В терминале IDEA из корня проекта запустить приложение: java -jar .\artifacts\aqa-shop.jar

Дождаться строки Started ShopApplication. Страница приложения: http://localhost:8080

Терминал с JAR оставить работающим.

## Запуск тестов и отчётов

В IDEA нажать **Ctrl два раза подряд**, чтобы открыть **Run Anything.**

Для полного прогона выполнить: gradle clean test --rerun-tasks

Команда clean удаляет предыдущие результаты из build. Параметр --rerun-tasks обеспечивает повторное выполнение тестов после переключения базы.

После завершения тестов открыть **Run Anything** и создать Allure-отчёт: gradle allureServe.

Для завершения просмотра остановить задачу allureServe в окне Run.

Перед повторным запуском тестов просмотр отчёта должен быть остановлен.

Падение тестов на обнаруженных несоответствиях приводит к BUILD FAILED задачи test.


# Запуск с PostgreSQL

Остановить задачу allureServe, если была запущена.

Остановить JAR сочетанием Ctrl+C в его терминале и дождаться завершения.

На ВМ выполнить:

docker stop diplomqa_mysqldb_1
docker-compose up -d postgresdb
docker logs --tail=30 diplomqa_postgresdb_1

Дождаться сообщения:database system is ready to accept connections

Порт ВМ 5432 должен быть доступен с компьютера. Эмулятор банка остаётся работать.

В application.properties на компьютере заменить только строку подключения: spring.datasource.url=jdbc:postgresql://VM_IP:5432/app

Подставить IP ВМ и сохранить файл. Остальные строки оставить прежними.

В терминале IDEA из корня проекта заново запустить приложение: java -jar .\artifacts\aqa-shop.jar

Дождаться Started ShopApplication.

Повторить полный прогон и просмотр отчета командами из раздела выше.

Базы данных запускаются по очереди.

## Тестовые данные

- 4444 4444 4444 4441 — карта со статусом APPROVED;

- 4444 4444 4444 4442 — карта со статусом DECLINED.

Корректный срок действия формируется относительно текущей даты. Негативные данные находятся в DataHelper.

SQL-проверки используют настройки из корневого application.properties. Приложение и тесты должны подключаться к одной базе.

# Завершение работы

Остановить JAR через **Ctrl+C** в его терминале.

Остановить задачу `allureServe` в окне **Run**.

При необходимости остановить контейнеры на ВМ: docker stop diplomqa_mysqldb_1 diplomqa_postgresdb_1 diplomqa_gate-simulator_1

