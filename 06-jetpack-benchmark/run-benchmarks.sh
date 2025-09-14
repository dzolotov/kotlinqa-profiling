#!/bin/bash

# Полный цикл запуска Jetpack Benchmark и извлечения результатов
# Использование: ./run-benchmarks.sh [micro|macro|all]

set -e

# Настройки
JAVA_HOME="/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home"
ANDROID_SDK_PATH="/Volumes/T7Shield/AndroidSdk"

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Определяем тип бенчмарков
BENCHMARK_TYPE=${1:-"all"}

echo -e "${PURPLE}🚀 Jetpack Benchmark - Полный цикл тестирования${NC}"
echo "=================================================="
echo -e "${BLUE}📋 Тип бенчмарков: $BENCHMARK_TYPE${NC}"
echo ""

# Функция очистки старых результатов
cleanup_old_results() {
    echo -e "${YELLOW}🧹 Очистка старых результатов на устройстве...${NC}"
    "$ANDROID_SDK_PATH/platform-tools/adb" shell "rm -rf /sdcard/Android/media/com.example.benchmark.test/additional_test_output/*" 2>/dev/null || true

    echo -e "${YELLOW}🧹 Очистка локальных результатов...${NC}"
    rm -rf ./benchmark-results/additional_test_output/* 2>/dev/null || true
}

# Функция запуска микробенчмарков
run_microbenchmarks() {
    echo -e "${GREEN}🔬 Запуск микробенчмарков (StringBenchmark)...${NC}"
    echo "=================================================="

    export JAVA_HOME="$JAVA_HOME"
    ./gradlew benchmark:connectedAndroidTest --no-daemon

    local EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        echo -e "${GREEN}✅ Микробенчмарки выполнены успешно!${NC}"
    else
        echo -e "${RED}❌ Ошибка при выполнении микробенчмарков (код: $EXIT_CODE)${NC}"
        return $EXIT_CODE
    fi
}

# Функция запуска макробенчмарков
run_macrobenchmarks() {
    echo -e "${GREEN}📱 Запуск макробенчмарков (StartupBenchmark)...${NC}"
    echo "=================================================="

    export JAVA_HOME="$JAVA_HOME"
    ./gradlew macrobenchmark:connectedAndroidTest --no-daemon

    local EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        echo -e "${GREEN}✅ Макробенчмарки выполнены успешно!${NC}"
    else
        echo -e "${RED}❌ Ошибка при выполнении макробенчмарков (код: $EXIT_CODE)${NC}"
        return $EXIT_CODE
    fi
}

# Проверяем наличие gradlew
if [ ! -f "./gradlew" ]; then
    echo -e "${RED}❌ gradlew не найден в текущей директории${NC}"
    echo -e "${YELLOW}💡 Убедитесь, что вы находитесь в корне проекта jetpack-benchmark${NC}"
    exit 1
fi

# Проверяем подключение устройства
echo -e "${BLUE}📱 Проверяем подключение устройства...${NC}"
DEVICES=$("$ANDROID_SDK_PATH/platform-tools/adb" devices | grep -v "List of devices attached" | grep -v "^$")

if [ -z "$DEVICES" ]; then
    echo -e "${RED}❌ Нет подключенных устройств или эмуляторов${NC}"
    echo -e "${YELLOW}💡 Запустите эмулятор командой:${NC}"
    echo "   export ANDROID_HOME=\"$ANDROID_SDK_PATH\""
    echo "   \$ANDROID_HOME/emulator/emulator -avd Otus_Emulator"
    exit 1
fi

echo -e "${GREEN}✅ Устройство найдено:${NC}"
echo "$DEVICES"

# Очищаем старые результаты
cleanup_old_results

# Запускаем бенчмарки в зависимости от типа
case $BENCHMARK_TYPE in
    "micro")
        run_microbenchmarks
        ;;
    "macro")
        run_macrobenchmarks
        ;;
    "all"|*)
        run_microbenchmarks
        if [ $? -eq 0 ]; then
            echo ""
            run_macrobenchmarks
        fi
        ;;
esac

# Проверяем успешность выполнения
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${PURPLE}⚡ Ожидание завершения записи результатов на устройство...${NC}"
    sleep 3

    echo -e "${BLUE}📁 Автоматическое извлечение trace файлов...${NC}"
    echo "=================================================="

    # Запускаем извлечение trace файлов
    if [ -f "./extract-traces.sh" ]; then
        ./extract-traces.sh
    else
        echo -e "${RED}❌ Скрипт extract-traces.sh не найден${NC}"
        echo -e "${YELLOW}💡 Создайте скрипт или извлеките файлы вручную${NC}"
    fi

    echo ""
    echo -e "${GREEN}🎉 Полный цикл бенчмарков завершен успешно!${NC}"
    echo "=================================================="
    echo -e "${BLUE}📊 Результаты доступны в:${NC}"
    echo "  • ./benchmark-results/additional_test_output/"
    echo "  • benchmark/build/reports/androidTests/"
    echo "  • macrobenchmark/build/reports/androidTests/"

else
    echo -e "${RED}❌ Ошибка при выполнении бенчмарков${NC}"
    echo -e "${YELLOW}💡 Проверьте логи выше для диагностики проблемы${NC}"
    exit 1
fi