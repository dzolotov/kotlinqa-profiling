#!/bin/bash

# Baseline Updater Script
# Обновляет baseline метрики производительности после значительных улучшений

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Конфигурация
BASELINE_DIR="${BASELINE_DIR:-baselines}"
IMPROVEMENT_THRESHOLD="${IMPROVEMENT_THRESHOLD:-5}"  # 5% улучшение для обновления baseline
BACKUP_COUNT="${BACKUP_COUNT:-5}"  # Количество backup файлов

echo -e "${PURPLE}🎯 Baseline Performance Updater${NC}"
echo "=================================================="

# Функция показа помощи
show_help() {
    cat << EOF
Использование: $0 [ОПЦИИ] CURRENT_RESULTS_FILE

Обновляет baseline метрики производительности на основе текущих результатов

ОПЦИИ:
    -h, --help              Показать эту справку
    -d, --baseline-dir DIR  Директория для хранения baseline файлов (по умолчанию: baselines)
    -t, --threshold PERCENT Порог улучшения для обновления baseline в % (по умолчанию: 5)
    -f, --force            Принудительно обновить baseline без проверки улучшений
    --dry-run              Показать что будет обновлено без реального обновления
    --backup-count N       Количество backup файлов для хранения (по умолчанию: 5)

ПРИМЕРЫ:
    $0 current-results.json
    $0 --threshold 10 --dry-run results.json
    $0 --force --baseline-dir ./perf-baselines results.json

ПЕРЕМЕННЫЕ ОКРУЖЕНИЯ:
    BASELINE_DIR           Директория baseline файлов
    IMPROVEMENT_THRESHOLD  Порог улучшения в процентах
    BACKUP_COUNT          Количество backup файлов
EOF
}

# Парсинг аргументов
DRY_RUN=false
FORCE_UPDATE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -d|--baseline-dir)
            BASELINE_DIR="$2"
            shift 2
            ;;
        -t|--threshold)
            IMPROVEMENT_THRESHOLD="$2"
            shift 2
            ;;
        -f|--force)
            FORCE_UPDATE=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --backup-count)
            BACKUP_COUNT="$2"
            shift 2
            ;;
        -*)
            echo -e "${RED}❌ Неизвестная опция: $1${NC}"
            show_help
            exit 1
            ;;
        *)
            CURRENT_RESULTS="$1"
            shift
            ;;
    esac
done

# Проверяем обязательные параметры
if [ -z "$CURRENT_RESULTS" ]; then
    echo -e "${RED}❌ Не указан файл с текущими результатами${NC}"
    show_help
    exit 1
fi

if [ ! -f "$CURRENT_RESULTS" ]; then
    echo -e "${RED}❌ Файл с результатами не найден: $CURRENT_RESULTS${NC}"
    exit 1
fi

echo -e "${BLUE}📁 Baseline directory: $BASELINE_DIR${NC}"
echo -e "${BLUE}📊 Current results: $CURRENT_RESULTS${NC}"
echo -e "${BLUE}⚡ Improvement threshold: $IMPROVEMENT_THRESHOLD%${NC}"

# Создаем директорию для baseline если не существует
mkdir -p "$BASELINE_DIR"

# Определяем тип результатов и baseline файл
determine_baseline_file() {
    local results_file="$1"
    local filename=$(basename "$results_file")

    case "$filename" in
        *jmh*|*JMH*)
            echo "$BASELINE_DIR/jmh-baseline.json"
            ;;
        *android*|*Android*)
            echo "$BASELINE_DIR/android-baseline.json"
            ;;
        *jvm*|*JVM*)
            echo "$BASELINE_DIR/jvm-baseline.json"
            ;;
        *js*|*JS*)
            echo "$BASELINE_DIR/js-baseline.json"
            ;;
        *native*|*Native*)
            echo "$BASELINE_DIR/native-baseline.json"
            ;;
        *)
            echo "$BASELINE_DIR/default-baseline.json"
            ;;
    esac
}

BASELINE_FILE=$(determine_baseline_file "$CURRENT_RESULTS")
echo -e "${BLUE}📋 Baseline file: $BASELINE_FILE${NC}"

# Функция создания backup
create_backup() {
    local file="$1"

    if [ -f "$file" ]; then
        local timestamp=$(date +%Y%m%d_%H%M%S)
        local backup_file="${file}.backup_${timestamp}"

        echo -e "${YELLOW}💾 Creating backup: $backup_file${NC}"
        cp "$file" "$backup_file"

        # Удаляем старые backup файлы, оставляем только последние N
        local backup_pattern="${file}.backup_*"
        local backup_files=($(ls -t $backup_pattern 2>/dev/null || true))

        if [ ${#backup_files[@]} -gt $BACKUP_COUNT ]; then
            echo -e "${YELLOW}🧹 Cleaning old backups (keeping $BACKUP_COUNT)${NC}"
            for ((i=$BACKUP_COUNT; i<${#backup_files[@]}; i++)); do
                echo -e "  Removing: ${backup_files[$i]}"
                if [ "$DRY_RUN" = false ]; then
                    rm -f "${backup_files[$i]}"
                fi
            done
        fi
    fi
}

# Функция сравнения производительности
compare_performance() {
    local current_file="$1"
    local baseline_file="$2"

    if [ ! -f "$baseline_file" ]; then
        echo -e "${YELLOW}⚠️ Baseline file not found, creating new baseline${NC}"
        return 0  # Новый baseline
    fi

    # Используем Python скрипт для сравнения
    local comparison_script="$(dirname "$0")/performance-comparison.py"

    if [ -f "$comparison_script" ]; then
        echo -e "${BLUE}📊 Comparing performance with baseline...${NC}"

        # Создаем временный файл для результатов сравнения
        local temp_comparison=$(mktemp)

        python3 "$comparison_script" \
            "$baseline_file" \
            "$current_file" \
            --format json \
            --warning-threshold 10 \
            --critical-threshold 30 \
            --output "$temp_comparison" 2>/dev/null || {
            echo -e "${YELLOW}⚠️ Could not compare with baseline (using simple update)${NC}"
            rm -f "$temp_comparison"
            return 0
        }

        # Парсим результаты сравнения
        local improvements=$(python3 -c "
import json
import sys
try:
    with open('$temp_comparison', 'r') as f:
        data = json.load(f)

    improvements = [c for c in data['comparisons'] if c['is_improvement']]
    significant_improvements = [c for c in improvements if abs(c['change_percent']) >= $IMPROVEMENT_THRESHOLD]

    print(f'{len(significant_improvements)} {len(improvements)} {len(data[\"comparisons\"])}')
except:
    print('0 0 0')
")

        read significant_count total_improvements total_benchmarks <<< "$improvements"

        rm -f "$temp_comparison"

        echo -e "${GREEN}✅ Performance comparison completed:${NC}"
        echo -e "  Total benchmarks: $total_benchmarks"
        echo -e "  Total improvements: $total_improvements"
        echo -e "  Significant improvements (≥$IMPROVEMENT_THRESHOLD%): $significant_count"

        # Определяем нужно ли обновлять baseline
        if [ "$significant_count" -gt 0 ] || [ "$FORCE_UPDATE" = true ]; then
            return 0  # Нужно обновить
        else
            return 1  # Обновление не требуется
        fi
    else
        echo -e "${YELLOW}⚠️ Performance comparison script not found, updating baseline${NC}"
        return 0
    fi
}

# Функция валидации JSON
validate_json() {
    local file="$1"

    python3 -c "
import json
import sys

try:
    with open('$file', 'r') as f:
        json.load(f)
    print('valid')
except:
    print('invalid')
" 2>/dev/null
}

# Функция получения статистики по результатам
get_results_stats() {
    local file="$1"

    python3 -c "
import json
import sys

try:
    with open('$file', 'r') as f:
        data = json.load(f)

    if isinstance(data, list):
        # JMH format
        print(f'JMH results: {len(data)} benchmarks')
    elif isinstance(data, dict):
        if 'benchmarks' in data:
            # Android Benchmark format
            print(f'Android results: {len(data[\"benchmarks\"])} benchmarks')
        elif 'results' in data:
            # kotlinx.benchmark format
            print(f'Kotlin results: {len(data[\"results\"])} benchmarks')
        else:
            print('Unknown format')
    else:
        print('Invalid format')
except Exception as e:
    print(f'Error: {e}')
"
}

# Основная логика
echo ""
echo -e "${BLUE}🔍 Analyzing current results...${NC}"

# Валидируем JSON
json_status=$(validate_json "$CURRENT_RESULTS")
if [ "$json_status" != "valid" ]; then
    echo -e "${RED}❌ Invalid JSON in current results file${NC}"
    exit 1
fi

# Показываем статистику
current_stats=$(get_results_stats "$CURRENT_RESULTS")
echo -e "${GREEN}📊 $current_stats${NC}"

# Сравниваем с baseline
echo ""
if compare_performance "$CURRENT_RESULTS" "$BASELINE_FILE"; then
    echo -e "${GREEN}🎯 Baseline update is recommended${NC}"

    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}🔍 DRY RUN: Would update baseline file: $BASELINE_FILE${NC}"
        if [ -f "$BASELINE_FILE" ]; then
            echo -e "${YELLOW}🔍 DRY RUN: Would create backup of existing baseline${NC}"
        fi
    else
        # Создаем backup существующего baseline
        create_backup "$BASELINE_FILE"

        # Обновляем baseline
        echo -e "${GREEN}📝 Updating baseline...${NC}"
        cp "$CURRENT_RESULTS" "$BASELINE_FILE"

        # Добавляем метаданные
        python3 -c "
import json
import sys
from datetime import datetime

try:
    with open('$BASELINE_FILE', 'r') as f:
        data = json.load(f)

    # Добавляем метаданные о baseline
    metadata = {
        'baseline_updated': datetime.now().isoformat(),
        'source_file': '$CURRENT_RESULTS',
        'improvement_threshold': $IMPROVEMENT_THRESHOLD,
        'updated_by': 'baseline-updater.sh'
    }

    if isinstance(data, dict):
        data['baseline_metadata'] = metadata
    else:
        # Для списков создаем wrapper
        data = {
            'results': data,
            'baseline_metadata': metadata
        }

    with open('$BASELINE_FILE', 'w') as f:
        json.dump(data, f, indent=2)

    print('✅ Metadata added to baseline')
except Exception as e:
    print(f'⚠️ Could not add metadata: {e}')
"

        echo -e "${GREEN}✅ Baseline updated successfully!${NC}"
        echo -e "${BLUE}📁 New baseline: $BASELINE_FILE${NC}"

        # Показываем статистику нового baseline
        baseline_stats=$(get_results_stats "$BASELINE_FILE")
        echo -e "${GREEN}📊 $baseline_stats${NC}"
    fi

elif [ "$FORCE_UPDATE" = true ]; then
    echo -e "${YELLOW}⚡ Force update requested${NC}"

    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}🔍 DRY RUN: Would force update baseline${NC}"
    else
        create_backup "$BASELINE_FILE"
        cp "$CURRENT_RESULTS" "$BASELINE_FILE"
        echo -e "${GREEN}✅ Baseline force updated!${NC}"
    fi

else
    echo -e "${BLUE}📊 No significant improvements detected${NC}"
    echo -e "${BLUE}💡 Current baseline is still optimal${NC}"

    if [ ! -f "$BASELINE_FILE" ]; then
        echo -e "${YELLOW}⚠️ But baseline file doesn't exist, creating initial baseline${NC}"

        if [ "$DRY_RUN" = false ]; then
            cp "$CURRENT_RESULTS" "$BASELINE_FILE"
            echo -e "${GREEN}✅ Initial baseline created${NC}"
        fi
    fi
fi

echo ""
echo -e "${PURPLE}📋 Summary:${NC}"
echo -e "  Current results: ${GREEN}$(basename "$CURRENT_RESULTS")${NC}"
echo -e "  Baseline file: ${GREEN}$(basename "$BASELINE_FILE")${NC}"
echo -e "  Improvement threshold: ${BLUE}$IMPROVEMENT_THRESHOLD%${NC}"

if [ -f "$BASELINE_FILE" ]; then
    echo -e "  Baseline last modified: ${BLUE}$(stat -f %Sm "$BASELINE_FILE" 2>/dev/null || date -r "$BASELINE_FILE" 2>/dev/null || echo "Unknown")${NC}"
fi

if [ "$DRY_RUN" = true ]; then
    echo -e "  ${YELLOW}🔍 DRY RUN MODE - No changes made${NC}"
fi

echo ""
echo -e "${GREEN}🎉 Baseline update process completed!${NC}"