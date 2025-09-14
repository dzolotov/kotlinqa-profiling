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
    echo -e "${BLUE}    JMH Antipatterns Demo       ${NC}"
    echo -e "${BLUE}         Kotlin Edition         ${NC}"
    echo -e "${BLUE}================================${NC}"
    echo ""
}

# Функция для печати меню
print_menu() {
    echo -e "${CYAN}Выберите действие:${NC}"
    echo -e "  ${GREEN}1${NC}) Быстрые тесты (smoke) - 30 секунд"
    echo -e "  ${GREEN}2${NC}) Полные тесты - 5-10 минут"
    echo -e "  ${GREEN}3${NC}) Тест Dead Code Elimination"
    echo -e "  ${GREEN}4${NC}) Тест Constant Folding"
    echo -e "  ${GREEN}5${NC}) Тест State & Scope"
    echo -e "  ${GREEN}6${NC}) Kotlin-специфичные проблемы"
    echo -e "  ${GREEN}7${NC}) Сравнение производительности"
    echo -e "  ${GREEN}8${NC}) Компиляция и подготовка"
    echo -e "  ${GREEN}9${NC}) Очистка результатов"
    echo -e "  ${RED}0${NC}) Выход"
    echo ""
}

# Проверяем наличие JMH JAR
find_jmh_jar() {
    local jar_file=$(find build/libs -name "*jmh.jar" 2>/dev/null | head -1)
    if [ -f "$jar_file" ]; then
        echo "$jar_file"
        return 0
    else
        return 1
    fi
}

# Функция для компиляции
compile_benchmarks() {
    echo -e "${YELLOW}Компилируем JMH бенчмарки...${NC}"
    if ./gradlew jmhJar --quiet; then
        echo -e "${GREEN}✓ Компиляция успешна${NC}"
        return 0
    else
        echo -e "${RED}✗ Ошибка компиляции${NC}"
        return 1
    fi
}

# Функция для быстрых тестов
run_smoke_tests() {
    echo -e "${YELLOW}Запускаем быстрые тесты (2 прогрева, 3 измерения)...${NC}"
    local jar_file=$(find_jmh_jar)
    if [ $? -eq 0 ]; then
        java -jar "$jar_file" \
            -wi 2 -i 3 -r 500ms -w 500ms -f 1 \
            -rf text -rff build/reports/smoke-results.txt
    else
        echo -e "${RED}✗ JMH JAR не найден${NC}"
    fi
}

# Функция для полных тестов
run_full_tests() {
    echo -e "${YELLOW}Запускаем полные тесты (5 прогревов, 10 измерений)...${NC}"
    echo -e "${PURPLE}Это займет 5-10 минут, наберитесь терпения...${NC}"
    local jar_file=$(find_jmh_jar)
    if [ $? -eq 0 ]; then
        java -jar "$jar_file" \
            -wi 5 -i 10 -r 1s -w 1s -f 1 \
            -rf text -rff build/reports/full-results.txt
    else
        echo -e "${RED}✗ JMH JAR не найден${NC}"
    fi
}

# Функция для конкретного теста
run_specific_test() {
    local test_pattern=$1
    local test_name=$2
    echo -e "${YELLOW}Запускаем тест: ${test_name}${NC}"
    local jar_file=$(find_jmh_jar)
    if [ $? -eq 0 ]; then
        java -jar "$jar_file" \
            -wi 3 -i 5 -r 1s -w 1s -f 1 \
            "$test_pattern" \
            -rf text
    else
        echo -e "${RED}✗ JMH JAR не найден${NC}"
    fi
}

# Функция для сравнения производительности
run_performance_comparison() {
    echo -e "${YELLOW}Сравниваем правильные и неправильные подходы...${NC}"
    local jar_file=$(find_jmh_jar)
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ JMH JAR не найден${NC}"
        return 1
    fi

    echo -e "${CYAN}=== Dead Code Elimination ===${NC}"
    java -jar "$jar_file" \
        ".*DeadCodeElimination.*(wrong|correct).*" \
        -wi 2 -i 3 -r 500ms -w 500ms -f 1 -rf text

    echo -e "${CYAN}=== Constant Folding ===${NC}"
    java -jar "$jar_file" \
        ".*ConstantFolding.*(wrong|correct).*" \
        -wi 2 -i 3 -r 500ms -w 500ms -f 1 -rf text
}

# Функция для очистки
clean_results() {
    echo -e "${YELLOW}Очищаем результаты и temporary файлы...${NC}"
    ./gradlew clean
    rm -rf build/reports/*.txt
    echo -e "${GREEN}✓ Очистка завершена${NC}"
}

# Функция для показа статуса
show_status() {
    echo -e "${CYAN}Статус проекта:${NC}"

    local jar_file=$(find_jmh_jar)
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ JMH JAR готов: $(basename "$jar_file")${NC}"
    else
        echo -e "${RED}✗ JMH JAR не найден${NC}"
    fi

    if [ -d "build/reports" ]; then
        local report_count=$(find build/reports -name "*.txt" 2>/dev/null | wc -l)
        echo -e "${GREEN}✓ Отчетов: $report_count${NC}"
    else
        echo -e "${YELLOW}! Папка отчетов не создана${NC}"
    fi

    echo ""
}

# Основной цикл
main() {
    clear
    print_header

    # Проверяем наличие Gradle
    if ! command -v ./gradlew &> /dev/null; then
        echo -e "${RED}Ошибка: gradlew не найден${NC}"
        exit 1
    fi

    while true; do
        show_status
        print_menu
        read -p "Ваш выбор: " choice

        case $choice in
            1)
                echo -e "${BLUE}--- Быстрые тесты ---${NC}"
                if compile_benchmarks; then
                    run_smoke_tests
                fi
                ;;
            2)
                echo -e "${BLUE}--- Полные тесты ---${NC}"
                if compile_benchmarks; then
                    run_full_tests
                fi
                ;;
            3)
                echo -e "${BLUE}--- Dead Code Elimination ---${NC}"
                if compile_benchmarks; then
                    run_specific_test ".*DeadCodeElimination.*" "Dead Code Elimination"
                fi
                ;;
            4)
                echo -e "${BLUE}--- Constant Folding ---${NC}"
                if compile_benchmarks; then
                    run_specific_test ".*ConstantFolding.*" "Constant Folding"
                fi
                ;;
            5)
                echo -e "${BLUE}--- State & Scope ---${NC}"
                if compile_benchmarks; then
                    run_specific_test ".*StateAndScope.*" "State & Scope"
                fi
                ;;
            6)
                echo -e "${BLUE}--- Kotlin-специфичные проблемы ---${NC}"
                if compile_benchmarks; then
                    run_specific_test ".*KotlinSpecificProblems.*" "Kotlin Specific Problems"
                fi
                ;;
            7)
                echo -e "${BLUE}--- Сравнение производительности ---${NC}"
                if compile_benchmarks; then
                    run_performance_comparison
                fi
                ;;
            8)
                echo -e "${BLUE}--- Компиляция ---${NC}"
                compile_benchmarks
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