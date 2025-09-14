#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║        Проверка системы мониторинга JVM               ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Проверка контейнеров
echo -e "${BLUE}[1] Статус контейнеров:${NC}"
docker-compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}" 2>/dev/null
echo ""

# Проверка метрик
echo -e "${BLUE}[2] Проверка экспорта JVM метрик:${NC}"
METRICS=$(curl -s http://localhost:9404/metrics 2>/dev/null | grep "^jvm_" | wc -l)
if [ "$METRICS" -gt 0 ]; then
    echo -e "${GREEN}✓ Найдено $METRICS JVM метрик${NC}"
    echo -e "  Примеры метрик:"
    curl -s http://localhost:9404/metrics | grep "^jvm_" | head -3 | sed 's/^/    /'
else
    echo -e "${RED}✗ JVM метрики не найдены${NC}"
fi
echo ""

# Проверка Prometheus
echo -e "${BLUE}[3] Статус Prometheus:${NC}"
PROM_STATUS=$(curl -s http://localhost:9090/api/v1/targets | python3 -c "
import json, sys
data = json.load(sys.stdin)
for target in data.get('data', {}).get('activeTargets', []):
    if target.get('labels', {}).get('job') == 'jvm-metrics':
        print(target.get('health', 'unknown'))
        break
" 2>/dev/null)

if [ "$PROM_STATUS" = "up" ]; then
    echo -e "${GREEN}✓ Prometheus успешно собирает метрики${NC}"
else
    echo -e "${RED}✗ Проблема со сбором метрик в Prometheus${NC}"
fi
echo ""

# Проверка Grafana
echo -e "${BLUE}[4] Статус Grafana:${NC}"
DASHBOARDS=$(curl -s -u admin:admin http://localhost:3000/api/search 2>/dev/null | python3 -c "
import json, sys
try:
    data = json.load(sys.stdin)
    count = len(data)
    print(f'{count}')
    for d in data:
        title = d.get('title', 'Unknown')
        uid = d.get('uid', 'unknown')
        panels = 'неизвестно'
        if uid == 'jvm-monitoring':
            panels = '5 панелей'
        elif uid == 'jvm-complete':
            panels = '13 панелей'
        print(f'  • {title} ({panels})')
except:
    print('0')
" 2>/dev/null)

DASHBOARD_COUNT=$(echo "$DASHBOARDS" | head -1)
if [ "$DASHBOARD_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Найдено дашбордов: $DASHBOARD_COUNT${NC}"
    echo "$DASHBOARDS" | tail -n +2
else
    echo -e "${RED}✗ Дашборды не найдены${NC}"
fi
echo ""

# Полезные ссылки
echo -e "${CYAN}════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}Полезные ссылки:${NC}"
echo ""
echo -e "${GREEN}📊 Grafana Dashboards:${NC}"
echo "   Полный дашборд:    http://localhost:3000/d/jvm-complete/jvm-complete-monitoring"
echo "   Базовый дашборд:   http://localhost:3000/d/jvm-monitoring/jvm-performance-monitoring"
echo "   Главная страница:  http://localhost:3000"
echo "   Логин: admin / Пароль: admin"
echo ""
echo -e "${YELLOW}📈 Prometheus:${NC}"
echo "   Главная:     http://localhost:9090"
echo "   Targets:     http://localhost:9090/targets"
echo "   Alerts:      http://localhost:9090/alerts"
echo ""
echo -e "${BLUE}📡 JVM Metrics:${NC}"
echo "   Все метрики: http://localhost:9404/metrics"
echo ""
echo -e "${CYAN}════════════════════════════════════════════════════════${NC}"