#!/bin/bash

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция для вывода с цветом
print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_info() {
    echo -e "${BLUE}[i]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

# Заголовок
echo -e "${BLUE}"
echo "╔════════════════════════════════════════════════════════╗"
echo "║         JVM Performance Monitoring Stack              ║"
echo "║                                                        ║"
echo "║  Java App + JMX + Prometheus + Grafana                ║"
echo "╚════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Проверка наличия Docker
if ! command -v docker &> /dev/null; then
    print_error "Docker не установлен!"
    echo "Установите Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

# Проверка наличия Docker Compose
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose не установлен!"
    echo "Установите Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi

# Проверка наличия Kotlin
if ! command -v kotlinc &> /dev/null; then
    print_warning "Kotlin компилятор не найден, пытаюсь установить через brew..."

    if command -v brew &> /dev/null; then
        brew install kotlin
        if [ $? -eq 0 ]; then
            print_status "Kotlin успешно установлен"
        else
            print_error "Не удалось установить Kotlin"
            exit 1
        fi
    else
        print_error "Homebrew не установлен. Установите Kotlin вручную:"
        echo "brew install kotlin"
        exit 1
    fi
fi

# Проверка наличия JAR файла
if [ ! -f "PerformanceTestApp.jar" ]; then
    print_info "JAR файл не найден, компилирую..."

    if [ -f "PerformanceTestApp.kt" ]; then
        kotlinc PerformanceTestApp.kt -include-runtime -d PerformanceTestApp.jar

        if [ $? -eq 0 ]; then
            print_status "Приложение успешно скомпилировано"
        else
            print_error "Ошибка компиляции"
            exit 1
        fi
    else
        print_error "Файл PerformanceTestApp.kt не найден!"
        exit 1
    fi
else
    print_status "JAR файл найден"
fi

# Остановка предыдущих контейнеров
print_info "Останавливаю предыдущие контейнеры..."
docker-compose down 2>/dev/null

# Запуск стека
print_info "Запускаю мониторинг стек..."
docker-compose up -d

if [ $? -eq 0 ]; then
    print_status "Все сервисы запущены!"

    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""

    # Ожидание готовности сервисов
    print_info "Ожидание запуска сервисов..."
    sleep 5

    # Проверка статуса контейнеров
    echo -e "${BLUE}Статус контейнеров:${NC}"
    docker-compose ps

    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""

    # Информация о доступе
    echo -e "${GREEN}🚀 Сервисы доступны по адресам:${NC}"
    echo ""
    echo -e "  ${BLUE}📊 Grafana:${NC}      http://localhost:3000"
    echo -e "                      Login: ${YELLOW}admin / admin${NC}"
    echo ""
    echo -e "  ${BLUE}📈 Дашборды:${NC}"
    echo -e "     • Базовый (5 панелей):  http://localhost:3000/d/jvm-monitoring/jvm-performance-monitoring"
    echo -e "     • Полный (13 панелей):  http://localhost:3000/d/jvm-complete/jvm-complete-monitoring"
    echo ""
    echo -e "  ${BLUE}🔍 Prometheus:${NC}   http://localhost:9090"
    echo ""
    echo -e "  ${BLUE}📡 JMX Metrics:${NC}  http://localhost:9404/metrics"
    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""

    # Команды управления
    echo -e "${YELLOW}Полезные команды:${NC}"
    echo ""
    echo "  Просмотр логов приложения:"
    echo "    docker-compose logs -f performance-app"
    echo ""
    echo "  Перезапуск приложения:"
    echo "    docker-compose restart performance-app"
    echo ""
    echo "  Остановка всех сервисов:"
    echo "    docker-compose down"
    echo ""
    echo "  Полная очистка (включая volumes):"
    echo "    docker-compose down -v"
    echo ""

    # Опция для открытия браузера
    if command -v open &> /dev/null; then
        echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo ""
        echo -e "${YELLOW}Какой дашборд открыть?${NC}"
        echo "  1) Полный дашборд (13 панелей) - рекомендуется"
        echo "  2) Базовый дашборд (5 панелей)"
        echo "  3) Главную страницу Grafana"
        echo "  4) Не открывать"
        echo ""
        read -p "Ваш выбор (1-4): " choice

        case $choice in
            1)
                sleep 3  # Даем время Grafana запуститься
                open "http://localhost:3000/d/jvm-complete/jvm-complete-monitoring"
                print_status "Открыт полный дашборд JVM мониторинга"
                ;;
            2)
                sleep 3
                open "http://localhost:3000/d/jvm-monitoring/jvm-performance-monitoring"
                print_status "Открыт базовый дашборд JVM мониторинга"
                ;;
            3)
                sleep 3
                open "http://localhost:3000"
                print_status "Открыта главная страница Grafana"
                ;;
            4)
                print_info "Браузер не открывается"
                ;;
            *)
                # По умолчанию открываем полный дашборд
                sleep 3
                open "http://localhost:3000/d/jvm-complete/jvm-complete-monitoring"
                print_status "Открыт полный дашборд JVM мониторинга (по умолчанию)"
                ;;
        esac
    fi

else
    print_error "Ошибка при запуске Docker Compose"
    exit 1
fi

echo ""
print_status "Мониторинг стек готов к работе!"