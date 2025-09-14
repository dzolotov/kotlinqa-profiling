FROM openjdk:17-jdk-slim

WORKDIR /app

# Установка необходимых утилит
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Скачиваем JMX Prometheus Java Agent
RUN wget -O jmx_prometheus_javaagent.jar \
    https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.19.0/jmx_prometheus_javaagent-0.19.0.jar

# Копируем скомпилированное приложение
COPY PerformanceTestApp.jar /app/
COPY jmx-config.yaml /app/

# Создаем директорию для логов
RUN mkdir -p /app/logs

# Порт приложения
EXPOSE 8080
# Порт для метрик
EXPOSE 9404

# Запуск с JMX Prometheus агентом в бесконечном цикле
CMD ["sh", "-c", "while true; do echo 'Starting application...'; java $JAVA_OPTS -jar PerformanceTestApp.jar || true; echo 'Application stopped, restarting in 5 seconds...'; sleep 5; done"]