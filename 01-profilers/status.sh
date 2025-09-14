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

# Функция проверки доступности сервиса
check_service() {
    local url=$1
    local name=$2

    if curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "200\|302"; then
        echo -e "  ${GREEN}✓${NC} $name: ${GREEN}ONLINE${NC} - $url"
    else
        echo -e "  ${RED}✗${NC} $name: ${RED}OFFLINE${NC} - $url"
    fi
}

# Заголовок
echo -e "${CYAN}"
echo "╔════════════════════════════════════════════════════════╗"
echo "║         Статус мониторинг стека                       ║"
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

# Проверка статуса контейнеров
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Контейнеры:${NC}"
echo ""

# Подсчет контейнеров
TOTAL=$(docker-compose ps -q 2>/dev/null | wc -l | tr -d ' ')
RUNNING=$(docker-compose ps -q 2>/dev/null | xargs -r -n1 docker inspect -f '{{.State.Running}}' | grep true | wc -l | tr -d ' ')

if [ "$TOTAL" -eq "0" ]; then
    print_error "Контейнеры не запущены"
    echo ""
    echo "Используйте ./start.sh для запуска"
    exit 1
fi

echo -e "  Всего: ${YELLOW}$TOTAL${NC} | Запущено: ${GREEN}$RUNNING${NC}"
echo ""

# Детальный статус каждого контейнера
docker-compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}" | while IFS= read -r line; do
    if echo "$line" | grep -q "Up"; then
        echo -e "  ${GREEN}✓${NC} $line" | sed 's/Up/\x1b[32mUp\x1b[0m/g'
    elif echo "$line" | grep -q "Exit"; then
        echo -e "  ${RED}✗${NC} $line" | sed 's/Exit/\x1b[31mExit\x1b[0m/g'
    else
        echo "  $line"
    fi
done

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Сервисы:${NC}"
echo ""

# Проверка доступности сервисов
check_service "http://localhost:3000" "Grafana"
check_service "http://localhost:9090" "Prometheus"
check_service "http://localhost:9404/metrics" "JMX Metrics"

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Метрики:${NC}"
echo ""

# Получение метрик из JMX Exporter
if curl -s "http://localhost:9404/metrics" > /dev/null 2>&1; then
    echo -e "${GREEN}JVM метрики доступны:${NC}"

    # Получаем некоторые ключевые метрики
    HEAP_USAGE=$(curl -s "http://localhost:9404/metrics" | grep "^jvm_memory_bytes_used{area=\"heap\"" | awk '{print $2}')
    THREADS=$(curl -s "http://localhost:9404/metrics" | grep "^jvm_threads_current" | awk '{print $2}')
    UPTIME=$(curl -s "http://localhost:9404/metrics" | grep "^process_uptime_seconds" | awk '{print $2}')

    if [ ! -z "$HEAP_USAGE" ]; then
        HEAP_MB=$(echo "scale=2; $HEAP_USAGE / 1024 / 1024" | bc 2>/dev/null || echo "N/A")
        echo -e "  • Heap Memory: ${YELLOW}${HEAP_MB} MB${NC}"
    fi

    if [ ! -z "$THREADS" ]; then
        echo -e "  • Active Threads: ${YELLOW}${THREADS}${NC}"
    fi

    if [ ! -z "$UPTIME" ]; then
        UPTIME_MIN=$(echo "scale=2; $UPTIME / 60" | bc 2>/dev/null || echo "N/A")
        echo -e "  • Uptime: ${YELLOW}${UPTIME_MIN} минут${NC}"
    fi
else
    print_error "JMX метрики недоступны"
fi

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Использование ресурсов:${NC}"
echo ""

# Статистика Docker
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" | while IFS= read -r line; do
    if echo "$line" | grep -q "performance-app\|prometheus\|grafana"; then
        echo "  $line"
    fi
done

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Быстрые ссылки
if [ "$RUNNING" -eq "$TOTAL" ] && [ "$TOTAL" -gt "0" ]; then
    echo -e "${GREEN}Все сервисы работают!${NC}"
    echo ""
    echo "Быстрый доступ:"
    echo "  • Grafana:    http://localhost:3000 (admin/admin)"
    echo "  • Prometheus: http://localhost:9090"
    echo "  • Metrics:    http://localhost:9404/metrics"
else
    print_error "Некоторые сервисы не работают"
    echo ""
    echo "Используйте ./start.sh для перезапуска"
fi

echo ""