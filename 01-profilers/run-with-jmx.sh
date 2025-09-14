#!/bin/bash

echo "Запуск PerformanceTestApp с JMX поддержкой..."

# JVM параметры для мониторинга
JVM_OPTS="-server \
-Xms512m \
-Xmx1g \
-XX:+UseG1GC \
-XX:+UseStringDeduplication \
-Dcom.sun.management.jmxremote=true \
-Dcom.sun.management.jmxremote.port=9999 \
-Dcom.sun.management.jmxremote.rmi.port=9999 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Djava.rmi.server.hostname=0.0.0.0 \
-XX:+FlightRecorder \
-XX:StartFlightRecording=duration=300s,filename=/app/logs/recording.jfr"

# Логирование GC для анализа
GC_OPTS="-Xlog:gc*:logs/gc.log:time,tags \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/app/logs/"

echo "JVM Options: $JVM_OPTS $GC_OPTS"
echo "Приложение будет доступно через JMX на порту 9999"
echo "JFR запись будет сохранена в /app/logs/recording.jfr"

# Автоматический запуск приложения через 5 секунд
(sleep 5; echo "") | java $JVM_OPTS $GC_OPTS -jar PerformanceTestApp.jar