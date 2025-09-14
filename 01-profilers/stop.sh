#!/bin/bash

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
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
echo -e "${CYAN}"
echo "╔════════════════════════════════════════════════════════╗"
echo "║         Остановка мониторинг стека                    ║"
echo "╚════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Проверка Docker
if ! command -v docker &> /dev/null; then
    print_error "Docker не установлен!"
    exit 1
fi

# Проверка Docker Compose
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose не установлен!"
    exit 1
fi

# Меню действий
echo -e "${YELLOW}Выберите действие:${NC}"
echo ""
echo "  1) Остановить контейнеры (сохранить данные)"
echo "  2) Остановить и удалить данные"
echo "  3) Перезапустить все контейнеры"
echo "  4) Показать логи перед остановкой"
echo "  5) Отмена"
echo ""

read -p "Ваш выбор (1-5): " choice

case $choice in
    1)
        print_info "Останавливаю контейнеры..."
        docker-compose down
        if [ $? -eq 0 ]; then
            print_status "Контейнеры остановлены"
            print_info "Данные сохранены в volumes"
        else
            print_error "Ошибка при остановке контейнеров"
        fi
        ;;

    2)
        print_warning "Это удалит все данные Prometheus и Grafana!"
        read -p "Вы уверены? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            print_info "Останавливаю контейнеры и удаляю данные..."
            docker-compose down -v
            if [ $? -eq 0 ]; then
                print_status "Контейнеры остановлены и данные удалены"
            else
                print_error "Ошибка при остановке контейнеров"
            fi
        else
            print_info "Операция отменена"
        fi
        ;;

    3)
        print_info "Перезапускаю контейнеры..."
        docker-compose restart
        if [ $? -eq 0 ]; then
            print_status "Контейнеры перезапущены"
            echo ""
            echo "Сервисы доступны по адресам:"
            echo "  • Grafana:    http://localhost:3000"
            echo "  • Prometheus: http://localhost:9090"
            echo "  • Metrics:    http://localhost:9404/metrics"
        else
            print_error "Ошибка при перезапуске контейнеров"
        fi
        ;;

    4)
        print_info "Показываю последние логи..."
        echo ""
        echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo -e "${BLUE}Performance App:${NC}"
        docker-compose logs --tail=10 performance-app
        echo ""
        echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo -e "${BLUE}Prometheus:${NC}"
        docker-compose logs --tail=10 prometheus
        echo ""
        echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo -e "${BLUE}Grafana:${NC}"
        docker-compose logs --tail=10 grafana
        echo ""

        # Рекурсивный вызов для повторного выбора
        exec "$0"
        ;;

    5)
        print_info "Операция отменена"
        exit 0
        ;;

    *)
        print_error "Неверный выбор!"
        exit 1
        ;;
esac

echo ""
