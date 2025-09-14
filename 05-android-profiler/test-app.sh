#!/bin/bash

# Скрипт для тестирования Android приложения профилировщика

set -e

echo "🧪 Тестируем Android Profiler приложение..."

# Проверяем подключение
echo "📱 Проверяем подключение к устройству..."
adb devices

# Запускаем приложение
echo "🚀 Запускаем приложение..."
adb shell am start -n com.example.profiler/.ProfiledActivity
sleep 2

echo "🎯 Запускаем профилировщик и симулируем нагрузку..."

# Включаем method tracing
PID=$(adb shell pidof com.example.profiler)
echo "📊 PID приложения: $PID"

echo "🔥 Симулируем CPU нагрузку через UI..."
# Кликаем на кнопку CPU test
adb shell input tap 540 400  # Примерная позиция кнопки CPU test

sleep 3

echo "💾 Симулируем Memory нагрузку..."
# Кликаем на кнопку Memory test
adb shell input tap 540 500  # Примерная позиция кнопки Memory test

sleep 3

echo "⚡ Симулируем Async нагрузку..."
# Кликаем на кнопку Async test
adb shell input tap 540 600  # Примерная позиция кнопки Async test

sleep 3

echo "📈 Показываем статистику..."
# Кликаем на кнопку Show Stats
adb shell input tap 540 700  # Примерная позиция кнопки Show Stats

echo "📋 Получаем логи приложения за последние 10 секунд..."
adb logcat -t 10 | grep -E "(CustomProfiler|ProfiledActivity)" || echo "Логи не найдены"

echo "💾 Создаем heap dump..."
adb shell am dumpheap $PID /data/local/tmp/heap-dump.hprof
sleep 2
adb pull /data/local/tmp/heap-dump.hprof . 2>/dev/null && echo "✅ Heap dump сохранен: heap-dump.hprof" || echo "❌ Не удалось создать heap dump"

echo ""
echo "🎉 Тестирование завершено!"
echo "📊 Приложение демонстрирует:"
echo "   - Кастомное профилирование методов"
echo "   - System tracing с Trace.beginSection/endSection"
echo "   - Method tracing с Debug.startMethodTracing"
echo "   - CPU-intensive операции (fibonacci)"
echo "   - Memory allocation patterns"
echo "   - Async операции с корутинами"
echo ""
echo "🔧 Для захвата системного трейса запустите: ./capture-trace.sh"