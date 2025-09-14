#!/bin/bash

# Скрипт автоматического извлечения trace файлов с Android устройства
# Использование: ./extract-traces.sh

set -e

# Настройки
ANDROID_SDK_PATH="/Volumes/T7Shield/AndroidSdk"
ADB_PATH="${ANDROID_SDK_PATH}/platform-tools/adb"
DEVICE_PATH="/sdcard/Android/media/com.example.benchmark.test/additional_test_output/"
LOCAL_PATH="./benchmark-results/additional_test_output/"
PACKAGE_NAME="com.example.benchmark.test"

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Автоматическое извлечение trace файлов Jetpack Benchmark${NC}"
echo "=================================================="

# Проверяем наличие ADB
if [ ! -f "$ADB_PATH" ]; then
    echo -e "${RED}❌ ADB не найден в $ADB_PATH${NC}"
    echo -e "${YELLOW}💡 Убедитесь, что Android SDK установлен в $ANDROID_SDK_PATH${NC}"
    exit 1
fi

# Проверяем подключение устройства
echo -e "${BLUE}📱 Проверяем подключение устройства...${NC}"
DEVICES=$("$ADB_PATH" devices | grep -v "List of devices attached" | grep -v "^$")

if [ -z "$DEVICES" ]; then
    echo -e "${RED}❌ Нет подключенных устройств или эмуляторов${NC}"
    echo -e "${YELLOW}💡 Запустите эмулятор или подключите устройство${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Устройство найдено:${NC}"
echo "$DEVICES"

# Создаем локальную директорию
echo -e "${BLUE}📁 Создаем локальную директорию...${NC}"
mkdir -p "$LOCAL_PATH"

# Проверяем наличие файлов на устройстве
echo -e "${BLUE}🔍 Проверяем наличие файлов на устройстве...${NC}"
FILES_ON_DEVICE=$("$ADB_PATH" shell "ls '$DEVICE_PATH' 2>/dev/null || echo 'NO_FILES'")

if [ "$FILES_ON_DEVICE" = "NO_FILES" ] || [ -z "$FILES_ON_DEVICE" ]; then
    echo -e "${RED}❌ Файлы не найдены на устройстве в $DEVICE_PATH${NC}"
    echo -e "${YELLOW}💡 Сначала запустите бенчмарки:${NC}"
    echo "   ./gradlew benchmark:connectedAndroidTest"
    echo "   ./gradlew macrobenchmark:connectedAndroidTest"
    exit 1
fi

echo -e "${GREEN}✅ Найдены файлы:${NC}"
echo "$FILES_ON_DEVICE"

# Подсчитываем количество файлов
FILE_COUNT=$(echo "$FILES_ON_DEVICE" | wc -l | xargs)
echo -e "${BLUE}📊 Всего файлов для извлечения: $FILE_COUNT${NC}"

# Извлекаем файлы
echo -e "${BLUE}⬇️  Извлекаем файлы...${NC}"
"$ADB_PATH" pull "$DEVICE_PATH" "$LOCAL_PATH" 2>/dev/null || {
    echo -e "${RED}❌ Ошибка при извлечении файлов${NC}"
    exit 1
}

# Проверяем результат
echo -e "${BLUE}✅ Проверяем результат извлечения...${NC}"
LOCAL_FILES=$(find "$LOCAL_PATH" -type f | wc -l | xargs)
echo -e "${GREEN}📁 Извлечено файлов: $LOCAL_FILES${NC}"

# Показываем детальную информацию о файлах
echo -e "${BLUE}📋 Детальная информация о файлах:${NC}"
echo "=================================================="

# Perfetto traces
PERFETTO_FILES=$(find "$LOCAL_PATH" -name "*.perfetto-trace" | head -5)
if [ -n "$PERFETTO_FILES" ]; then
    echo -e "${GREEN}🔥 Perfetto Traces (для Android Studio Profiler):${NC}"
    while IFS= read -r file; do
        if [ -f "$file" ]; then
            SIZE=$(du -h "$file" | cut -f1)
            FILENAME=$(basename "$file")
            echo "  • $FILENAME ($SIZE)"
        fi
    done <<< "$PERFETTO_FILES"
fi

# Method traces
METHOD_FILES=$(find "$LOCAL_PATH" -name "*.trace" | head -5)
if [ -n "$METHOD_FILES" ]; then
    echo -e "${GREEN}⚡ Method Traces (для детального анализа):${NC}"
    while IFS= read -r file; do
        if [ -f "$file" ]; then
            SIZE=$(du -h "$file" | cut -f1)
            FILENAME=$(basename "$file")
            echo "  • $FILENAME ($SIZE)"
        fi
    done <<< "$METHOD_FILES"
fi

# JSON результаты
JSON_FILES=$(find "$LOCAL_PATH" -name "*.json" | head -3)
if [ -n "$JSON_FILES" ]; then
    echo -e "${GREEN}📊 JSON Результаты (статистика):${NC}"
    while IFS= read -r file; do
        if [ -f "$file" ]; then
            SIZE=$(du -h "$file" | cut -f1)
            FILENAME=$(basename "$file")
            echo "  • $FILENAME ($SIZE)"
        fi
    done <<< "$JSON_FILES"
fi

# Подсчитываем общий размер
TOTAL_SIZE=$(du -sh "$LOCAL_PATH" | cut -f1)
echo "=================================================="
echo -e "${GREEN}💾 Общий размер извлеченных данных: $TOTAL_SIZE${NC}"

# Инструкции по использованию
echo ""
echo -e "${BLUE}📖 Инструкции по использованию:${NC}"
echo "=================================================="
echo -e "${YELLOW}1. Perfetto Traces:${NC}"
echo "   • Откройте Android Studio → View → Tool Windows → Profiler"
echo "   • Drag & drop .perfetto-trace файлы для анализа"
echo ""
echo -e "${YELLOW}2. Method Traces:${NC}"
echo "   • Откройте .trace файлы в Android Studio Profiler"
echo "   • Или используйте утилиту traceview из Android SDK"
echo ""
echo -e "${YELLOW}3. JSON Результаты:${NC}"
echo "   • Откройте в текстовом редакторе или парсите программно"
echo "   • Содержат медиану, min/max, все измерения"
echo ""
echo -e "${GREEN}🎉 Извлечение завершено успешно!${NC}"

# Опционально: открываем директорию с результатами
if command -v open &> /dev/null; then
    echo ""
    read -p "Открыть папку с результатами? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        open "$LOCAL_PATH"
    fi
fi