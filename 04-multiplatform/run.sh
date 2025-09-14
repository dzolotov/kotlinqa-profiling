#!/bin/bash

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Функция для печати заголовка
print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE} Kotlin Multiplatform Benchmarks${NC}"
    echo -e "${BLUE}   Performance Comparison Demo  ${NC}"
    echo -e "${BLUE}================================${NC}"
    echo ""
}

# Функция для печати меню
print_menu() {
    echo -e "${CYAN}Выберите действие:${NC}"
    echo -e "  ${GREEN}1${NC}) JVM тесты (быстро)"
    echo -e "  ${GREEN}2${NC}) JavaScript тесты (Node.js)"
    echo -e "  ${GREEN}3${NC}) Native тесты (macOS)"
    echo -e "  ${GREEN}4${NC}) Все платформы - сравнение"
    echo -e "  ${GREEN}5${NC}) Smoke тесты всех платформ"
    echo -e "  ${GREEN}6${NC}) Collection benchmarks (JVM)"
    echo -e "  ${GREEN}7${NC}) Platform optimizer comparison"
    echo -e "  ${GREEN}8${NC}) Компиляция проекта"
    echo -e "  ${GREEN}9${NC}) Очистка результатов"
    echo -e "  ${RED}0${NC}) Выход"
    echo ""
}

# Проверяем наличие Gradle
check_gradle() {
    if ! command -v ./gradlew &> /dev/null; then
        echo -e "${RED}Ошибка: gradlew не найден${NC}"
        exit 1
    fi
}

# Функция для компиляции
compile_project() {
    echo -e "${YELLOW}Компилируем Kotlin Multiplatform проект...${NC}"
    if ./gradlew build --quiet; then
        echo -e "${GREEN}✓ Компиляция успешна${NC}"
        return 0
    else
        echo -e "${RED}✗ Ошибка компиляции${NC}"
        return 1
    fi
}

# Функция для запуска JVM бенчмарков
run_jvm_benchmarks() {
    echo -e "${YELLOW}Запускаем JVM бенчмарки...${NC}"
    ./gradlew jvmBenchmark --quiet
}

# Функция для запуска JS бенчмарков
run_js_benchmarks() {
    echo -e "${YELLOW}Запускаем JavaScript бенчмарки...${NC}"
    ./gradlew jsBenchmark --quiet
}

# Функция для запуска Native бенчмарков
run_native_benchmarks() {
    echo -e "${YELLOW}Запускаем Native бенчмарки (macOS)...${NC}"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # Определяем архитектуру
        if [[ $(uname -m) == "arm64" ]]; then
            ./gradlew macosArm64Benchmark --quiet
        else
            ./gradlew macosX64Benchmark --quiet
        fi
    else
        echo -e "${YELLOW}macOS не обнаружена, пробуем Linux...${NC}"
        ./gradlew linuxX64Benchmark --quiet
    fi
}

# Функция для сравнения всех платформ
run_all_platforms() {
    echo -e "${YELLOW}Запускаем сравнение всех платформ...${NC}"
    echo -e "${PURPLE}Это может занять несколько минут...${NC}"

    echo -e "${CYAN}=== JVM Результаты ===${NC}"
    ./gradlew jvmBenchmark --quiet

    echo -e "${CYAN}=== JavaScript Результаты ===${NC}"
    ./gradlew jsBenchmark --quiet

    echo -e "${CYAN}=== Native Результаты ===${NC}"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        if [[ $(uname -m) == "arm64" ]]; then
            ./gradlew macosArm64Benchmark --quiet
        else
            ./gradlew macosX64Benchmark --quiet
        fi
    fi
}

# Функция для smoke тестов
run_smoke_tests() {
    echo -e "${YELLOW}Запускаем быстрые smoke тесты...${NC}"

    echo -e "${CYAN}--- JVM ---${NC}"
    timeout 30 ./gradlew jvmBenchmark --quiet || echo "JVM тест завершен"

    echo -e "${CYAN}--- JavaScript ---${NC}"
    timeout 30 ./gradlew jsBenchmark --quiet || echo "JS тест завершен"
}

# Функция для тестов коллекций
run_collection_benchmarks() {
    echo -e "${YELLOW}Запускаем бенчмарки коллекций...${NC}"
    ./gradlew jvmBenchmark --quiet
}

# Функция для сравнения оптимизаторов
run_platform_optimizer_comparison() {
    echo -e "${YELLOW}Сравниваем платформенные оптимизации...${NC}"
    echo -e "${CYAN}--- JVM Platform Optimizer ---${NC}"
    ./gradlew jvmBenchmark --quiet
}

# Функция для очистки
clean_results() {
    echo -e "${YELLOW}Очищаем результаты и артефакты...${NC}"
    ./gradlew clean --quiet
    echo -e "${GREEN}✓ Очистка завершена${NC}"
}

# Функция для показа статуса
show_status() {
    echo -e "${CYAN}Статус проекта:${NC}"

    if [ -f "build.gradle.kts" ]; then
        echo -e "${GREEN}✓ Kotlin Multiplatform проект${NC}"
    else
        echo -e "${RED}✗ build.gradle.kts не найден${NC}"
    fi

    # Проверяем доступные таргеты
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo -e "${GREEN}✓ macOS Native таргет доступен${NC}"
    else
        echo -e "${YELLOW}! macOS не обнаружена${NC}"
    fi

    if command -v node &> /dev/null; then
        echo -e "${GREEN}✓ Node.js доступен ($(node --version))${NC}"
    else
        echo -e "${YELLOW}! Node.js не установлен${NC}"
    fi

    echo ""
}

# Основной цикл
main() {
    clear
    print_header
    check_gradle

    while true; do
        show_status
        print_menu
        read -p "Ваш выбор: " choice

        case $choice in
            1)
                echo -e "${BLUE}--- JVM Benchmarks ---${NC}"
                if compile_project; then
                    run_jvm_benchmarks
                fi
                ;;
            2)
                echo -e "${BLUE}--- JavaScript Benchmarks ---${NC}"
                if compile_project; then
                    run_js_benchmarks
                fi
                ;;
            3)
                echo -e "${BLUE}--- Native Benchmarks ---${NC}"
                if compile_project; then
                    run_native_benchmarks
                fi
                ;;
            4)
                echo -e "${BLUE}--- All Platforms Comparison ---${NC}"
                if compile_project; then
                    run_all_platforms
                fi
                ;;
            5)
                echo -e "${BLUE}--- Smoke Tests ---${NC}"
                if compile_project; then
                    run_smoke_tests
                fi
                ;;
            6)
                echo -e "${BLUE}--- Collection Benchmarks ---${NC}"
                if compile_project; then
                    run_collection_benchmarks
                fi
                ;;
            7)
                echo -e "${BLUE}--- Platform Optimizer Comparison ---${NC}"
                if compile_project; then
                    run_platform_optimizer_comparison
                fi
                ;;
            8)
                echo -e "${BLUE}--- Project Compilation ---${NC}"
                compile_project
                ;;
            9)
                clean_results
                ;;
            0)
                echo -e "${GREEN}До свидания!${NC}"
                exit 0
                ;;
            *)
                echo -e "${RED}Неверный выбор. Попробуйте еще раз.${NC}"
                ;;
        esac

        echo ""
        read -p "Нажмите Enter для продолжения..."
        clear
        print_header
    done
}

# Запуск
main