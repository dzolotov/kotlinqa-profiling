#!/bin/bash

# Скрипт для захвата системного трейса Android приложения

set -e

echo "🎯 Захват системного трейса для профилировщика Android..."

# Настройки трейсинга
TRACE_TIME=10
BUFFER_SIZE=32768
OUTPUT_FILE="android-profiler-trace.html"

echo "📱 Проверяем подключение к устройству..."
adb devices

echo "🔍 Проверяем, что приложение запущено..."
adb shell "am force-stop com.example.profiler"
sleep 1
adb shell "am start -n com.example.profiler/.ProfiledActivity"
sleep 2

echo "📊 Запускаем системный трейс на ${TRACE_TIME} секунд..."
adb shell "atrace -t ${TRACE_TIME} -b ${BUFFER_SIZE} -o /data/local/tmp/${OUTPUT_FILE} \
  view input sched freq idle disk sync workq irq regulators \
  binder_driver dalvik database gfx memory power \
  webview wm am sm audio video camera hal res"

echo "📥 Скачиваем файл трейса..."
adb pull "/data/local/tmp/${OUTPUT_FILE}" .

echo "🧹 Очищаем временный файл на устройстве..."
adb shell "rm /data/local/tmp/${OUTPUT_FILE}"

echo "✅ Трейс сохранен в файл: ${OUTPUT_FILE}"
echo "🌐 Откройте chrome://tracing в Chrome и загрузите файл ${OUTPUT_FILE}"
echo ""
echo "📝 Для анализа Method Tracing:"
echo "   adb shell am dumpheap <PID> /data/local/tmp/heap.hprof"
echo "   adb shell am profile start com.example.profiler /data/local/tmp/profile.trace"
echo "   adb shell am profile stop com.example.profiler"