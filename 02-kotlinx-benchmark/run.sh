#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_info() {
    echo -e "${BLUE}[i]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# Заголовок
echo -e "${CYAN}"
echo "╔════════════════════════════════════════════════════════╗"
echo "║              Kotlinx.benchmark Demo                    ║"
echo "║                                                        ║"
echo "║    Современные микробенчмарки для Kotlin              ║"
echo "╚════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Проверка Java
if ! command -v java &> /dev/null; then
    print_error "Java не найден!"
    exit 1
fi

# Проверка Gradle Wrapper
if [ ! -f "./gradlew" ]; then
    print_warning "Gradle Wrapper не найден, используем системный gradle"
    GRADLE_CMD="gradle"
else
    GRADLE_CMD="./gradlew"
fi

# Меню выбора конфигурации
echo -e "${YELLOW}Выберите конфигурацию бенчмарка:${NC}"
echo ""
echo "  1) Smoke тест (быстрые бенчмарки) - 500ms итерации"
echo "  2) Main (полные бенчмарки) - 10s итерации"
echo "  3) Только компиляция"
echo "  4) Показать отчеты"
echo ""

read -p "Ваш выбор (1-4): " choice

case $choice in
    1)
        CONFIG="smoke"
        print_info "Запуск быстрых бенчмарков (smoke)..."
        $GRADLE_CMD jvmSmokeBenchmark
        ;;

    2)
        CONFIG="main"
        print_info "Запуск полных бенчмарков (main)..."
        $GRADLE_CMD jvmBenchmark
        ;;

    3)
        print_info "Только компиляция бенчмарков..."
        $GRADLE_CMD jvmBenchmarkCompile
        ;;

    4)
        print_info "Показ последних отчетов..."
        if [ -d "build/reports/benchmarks" ]; then
            find build/reports/benchmarks -name "*.json" -exec basename {} \; | sort
            echo ""
            echo "Файлы отчетов:"
            find build/reports/benchmarks -name "*" -type f | head -10

            # Поиск последнего текстового отчета
            LATEST_REPORT=$(find build/reports/benchmarks -name "*.txt" | head -1)
            if [ -n "$LATEST_REPORT" ]; then
                echo ""
                echo -e "${GREEN}Последний текстовый отчет:${NC}"
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                cat "$LATEST_REPORT" | head -50
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            fi
        else
            print_warning "Отчеты не найдены. Сначала запустите бенчмарки."
        fi
        exit 0
        ;;

    *)
        print_error "Неверный выбор!"
        exit 1
        ;;
esac

# Проверка результатов
if [ $? -eq 0 ]; then
    print_status "Бенчмарки завершены успешно!"

    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""

    # Показ структуры отчетов
    if [ -d "build/reports/benchmarks" ]; then
        echo -e "${BLUE}📊 Отчеты сохранены в:${NC}"
        echo "   build/reports/benchmarks/"
        echo ""

        # Список отчетов
        find build/reports/benchmarks -name "*.json" | head -5 | while read file; do
            echo "   📋 $(basename "$file")"
        done

        # Показ краткого результата из последнего отчета
        LATEST_TXT=$(find build/reports/benchmarks -name "*.txt" | head -1)
        if [ -n "$LATEST_TXT" ]; then
            echo ""
            echo -e "${YELLOW}📈 Краткие результаты (первые строки):${NC}"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            head -20 "$LATEST_TXT" | grep -E "(Benchmark|Score|Mode)" || head -10 "$LATEST_TXT"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo ""
            echo "Полный отчет: $LATEST_TXT"
        fi
    fi

    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""

    # Полезные команды
    echo -e "${YELLOW}💡 Полезные команды:${NC}"
    echo ""
    echo "  Повторный запуск:"
    echo "    ./run.sh"
    echo ""
    echo "  Просмотр всех отчетов:"
    echo "    ls -la build/reports/benchmarks/"
    echo ""
    echo "  Очистка результатов:"
    echo "    $GRADLE_CMD clean"
    echo ""
    echo "  Список всех задач:"
    echo "    $GRADLE_CMD tasks --all | grep -i benchmark"

else
    print_error "Ошибка при выполнении бенчмарков!"
    echo ""
    echo "Возможные причины:"
    echo "  • Ошибка компиляции"
    echo "  • Недостаточно памяти JVM"
    echo "  • Проблемы с зависимостями"
    echo ""
    echo "Попробуйте:"
    echo "  1) $GRADLE_CMD clean"
    echo "  2) $GRADLE_CMD compileJvmBenchmarkKotlin"
    echo "  3) Запустите снова: ./run.sh"

    exit 1
fi

print_status "Готово!"