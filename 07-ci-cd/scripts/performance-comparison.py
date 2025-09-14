#!/usr/bin/env python3
"""
Performance Comparison Script
Сравнивает результаты бенчмарков с baseline и определяет регрессии
"""

import json
import sys
import os
import argparse
from typing import Dict, List, Tuple, Optional
from dataclasses import dataclass
from pathlib import Path

@dataclass
class BenchmarkResult:
    name: str
    score: float
    error: float
    unit: str
    mode: str

@dataclass
class PerformanceComparison:
    benchmark_name: str
    baseline_score: float
    current_score: float
    change_percent: float
    is_regression: bool
    is_improvement: bool
    severity: str  # 'minor', 'warning', 'critical'

class PerformanceAnalyzer:
    def __init__(self, warning_threshold: float = 20.0, critical_threshold: float = 50.0):
        """
        Args:
            warning_threshold: Процент замедления для warning (20.0 = 20%)
            critical_threshold: Процент замедления для critical alert (50.0 = 50%)
        """
        self.warning_threshold = warning_threshold
        self.critical_threshold = critical_threshold

    def load_benchmark_results(self, file_path: str) -> List[BenchmarkResult]:
        """Загружает результаты бенчмарков из JSON файла"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)

            results = []

            # Обрабатываем разные форматы результатов
            if isinstance(data, list):
                # JMH формат
                for item in data:
                    if 'benchmark' in item and 'primaryMetric' in item:
                        metric = item['primaryMetric']
                        results.append(BenchmarkResult(
                            name=item['benchmark'],
                            score=metric['score'],
                            error=metric.get('scoreError', 0.0),
                            unit=metric['scoreUnit'],
                            mode=item.get('mode', 'unknown')
                        ))
            elif isinstance(data, dict):
                # Android Benchmark формат
                if 'benchmarks' in data:
                    for benchmark in data['benchmarks']:
                        if 'metrics' in benchmark and 'timeNs' in benchmark['metrics']:
                            time_metric = benchmark['metrics']['timeNs']
                            results.append(BenchmarkResult(
                                name=benchmark['name'],
                                score=time_metric['median'],
                                error=time_metric['maximum'] - time_metric['minimum'],
                                unit='ns',
                                mode='avgt'
                            ))
                # kotlinx.benchmark формат
                elif 'results' in data:
                    for result in data['results']:
                        results.append(BenchmarkResult(
                            name=result['benchmark'],
                            score=result['score'],
                            error=result.get('error', 0.0),
                            unit=result['unit'],
                            mode=result.get('mode', 'avgt')
                        ))

            return results

        except Exception as e:
            print(f"❌ Ошибка загрузки файла {file_path}: {e}")
            return []

    def compare_results(self, baseline: List[BenchmarkResult],
                       current: List[BenchmarkResult]) -> List[PerformanceComparison]:
        """Сравнивает текущие результаты с baseline"""

        # Создаем словари для быстрого поиска
        baseline_dict = {r.name: r for r in baseline}
        current_dict = {r.name: r for r in current}

        comparisons = []

        # Находим общие бенчмарки
        common_benchmarks = set(baseline_dict.keys()) & set(current_dict.keys())

        for benchmark_name in common_benchmarks:
            baseline_result = baseline_dict[benchmark_name]
            current_result = current_dict[benchmark_name]

            # Вычисляем процент изменения
            change_percent = ((current_result.score - baseline_result.score) / baseline_result.score) * 100

            # Для времени исполнения: увеличение = регрессия
            # Для throughput: уменьшение = регрессия
            is_time_metric = 'time' in baseline_result.unit.lower() or 'ns' in baseline_result.unit.lower()

            if is_time_metric:
                is_regression = change_percent > 0
                is_improvement = change_percent < -5  # Улучшение более 5%
            else:
                is_regression = change_percent < 0
                is_improvement = change_percent > 5

            # Определяем severity
            severity = 'minor'
            if is_regression:
                abs_change = abs(change_percent)
                if abs_change > self.critical_threshold:
                    severity = 'critical'
                elif abs_change > self.warning_threshold:
                    severity = 'warning'

            comparisons.append(PerformanceComparison(
                benchmark_name=benchmark_name,
                baseline_score=baseline_result.score,
                current_score=current_result.score,
                change_percent=change_percent,
                is_regression=is_regression,
                is_improvement=is_improvement,
                severity=severity
            ))

        return comparisons

    def generate_report(self, comparisons: List[PerformanceComparison],
                       output_format: str = 'markdown') -> str:
        """Генерирует отчет о сравнении производительности"""

        if not comparisons:
            return "Нет данных для сравнения"

        # Группируем по severity
        critical_regressions = [c for c in comparisons if c.severity == 'critical']
        warning_regressions = [c for c in comparisons if c.severity == 'warning' and c.is_regression]
        improvements = [c for c in comparisons if c.is_improvement]
        minor_changes = [c for c in comparisons if c.severity == 'minor']

        if output_format == 'markdown':
            return self._generate_markdown_report(
                critical_regressions, warning_regressions, improvements, minor_changes
            )
        elif output_format == 'json':
            return self._generate_json_report(comparisons)
        else:
            return self._generate_text_report(
                critical_regressions, warning_regressions, improvements, minor_changes
            )

    def _generate_markdown_report(self, critical: List[PerformanceComparison],
                                warnings: List[PerformanceComparison],
                                improvements: List[PerformanceComparison],
                                minor: List[PerformanceComparison]) -> str:
        """Генерирует Markdown отчет"""

        report = ["# 📊 Performance Comparison Report", ""]

        # Сводка
        total = len(critical) + len(warnings) + len(improvements) + len(minor)
        report.extend([
            "## 📋 Summary",
            "",
            f"- **Total benchmarks compared**: {total}",
            f"- 🚨 **Critical regressions**: {len(critical)}",
            f"- ⚠️ **Warning regressions**: {len(warnings)}",
            f"- ✅ **Improvements**: {len(improvements)}",
            f"- 📊 **Minor changes**: {len(minor)}",
            ""
        ])

        # Критические регрессии
        if critical:
            report.extend([
                "## 🚨 Critical Performance Regressions",
                "",
                "| Benchmark | Baseline | Current | Change | Severity |",
                "|-----------|----------|---------|---------|----------|"
            ])

            for comp in critical:
                report.append(
                    f"| `{comp.benchmark_name}` | {comp.baseline_score:.2f} | "
                    f"{comp.current_score:.2f} | **{comp.change_percent:+.1f}%** | 🚨 Critical |"
                )
            report.append("")

        # Warning регрессии
        if warnings:
            report.extend([
                "## ⚠️ Performance Warnings",
                "",
                "| Benchmark | Baseline | Current | Change |",
                "|-----------|----------|---------|--------|"
            ])

            for comp in warnings:
                report.append(
                    f"| `{comp.benchmark_name}` | {comp.baseline_score:.2f} | "
                    f"{comp.current_score:.2f} | **{comp.change_percent:+.1f}%** |"
                )
            report.append("")

        # Улучшения
        if improvements:
            report.extend([
                "## ✅ Performance Improvements",
                "",
                "| Benchmark | Baseline | Current | Improvement |",
                "|-----------|----------|---------|------------|"
            ])

            for comp in improvements:
                report.append(
                    f"| `{comp.benchmark_name}` | {comp.baseline_score:.2f} | "
                    f"{comp.current_score:.2f} | **{comp.change_percent:+.1f}%** |"
                )
            report.append("")

        # Рекомендации
        report.extend([
            "## 🎯 Recommendations",
            ""
        ])

        if critical:
            report.append("- 🚨 **Critical regressions detected** - immediate investigation required")
        if warnings:
            report.append("- ⚠️ **Performance warnings** - consider optimization")
        if improvements:
            report.append("- ✅ **Performance improvements detected** - consider updating baseline")
        if not critical and not warnings:
            report.append("- ✅ **No significant performance regressions detected**")

        return "\\n".join(report)

    def _generate_json_report(self, comparisons: List[PerformanceComparison]) -> str:
        """Генерирует JSON отчет"""

        data = {
            "summary": {
                "total_benchmarks": len(comparisons),
                "critical_regressions": len([c for c in comparisons if c.severity == 'critical']),
                "warning_regressions": len([c for c in comparisons if c.severity == 'warning' and c.is_regression]),
                "improvements": len([c for c in comparisons if c.is_improvement]),
                "minor_changes": len([c for c in comparisons if c.severity == 'minor'])
            },
            "comparisons": [
                {
                    "benchmark": comp.benchmark_name,
                    "baseline_score": comp.baseline_score,
                    "current_score": comp.current_score,
                    "change_percent": comp.change_percent,
                    "is_regression": comp.is_regression,
                    "is_improvement": comp.is_improvement,
                    "severity": comp.severity
                }
                for comp in comparisons
            ]
        }

        return json.dumps(data, indent=2)

    def _generate_text_report(self, critical: List[PerformanceComparison],
                            warnings: List[PerformanceComparison],
                            improvements: List[PerformanceComparison],
                            minor: List[PerformanceComparison]) -> str:
        """Генерирует текстовый отчет"""

        lines = ["Performance Comparison Report", "=" * 40, ""]

        total = len(critical) + len(warnings) + len(improvements) + len(minor)
        lines.extend([
            f"Total benchmarks: {total}",
            f"Critical regressions: {len(critical)}",
            f"Warning regressions: {len(warnings)}",
            f"Improvements: {len(improvements)}",
            f"Minor changes: {len(minor)}",
            ""
        ])

        if critical:
            lines.extend(["CRITICAL REGRESSIONS:", "-" * 20])
            for comp in critical:
                lines.append(f"{comp.benchmark_name}: {comp.change_percent:+.1f}% change")
            lines.append("")

        if warnings:
            lines.extend(["WARNING REGRESSIONS:", "-" * 18])
            for comp in warnings:
                lines.append(f"{comp.benchmark_name}: {comp.change_percent:+.1f}% change")
            lines.append("")

        if improvements:
            lines.extend(["IMPROVEMENTS:", "-" * 12])
            for comp in improvements:
                lines.append(f"{comp.benchmark_name}: {comp.change_percent:+.1f}% improvement")

        return "\\n".join(lines)

def main():
    parser = argparse.ArgumentParser(description='Compare performance benchmark results')
    parser.add_argument('baseline', help='Baseline benchmark results JSON file')
    parser.add_argument('current', help='Current benchmark results JSON file')
    parser.add_argument('--output', '-o', help='Output file (stdout if not specified)')
    parser.add_argument('--format', '-f', choices=['markdown', 'json', 'text'],
                       default='markdown', help='Output format')
    parser.add_argument('--warning-threshold', type=float, default=20.0,
                       help='Warning threshold percentage (default: 20.0)')
    parser.add_argument('--critical-threshold', type=float, default=50.0,
                       help='Critical threshold percentage (default: 50.0)')
    parser.add_argument('--fail-on-regression', action='store_true',
                       help='Exit with code 1 if critical regressions detected')

    args = parser.parse_args()

    # Проверяем существование файлов
    if not os.path.exists(args.baseline):
        print(f"❌ Baseline file not found: {args.baseline}")
        sys.exit(1)

    if not os.path.exists(args.current):
        print(f"❌ Current results file not found: {args.current}")
        sys.exit(1)

    # Создаем анализатор
    analyzer = PerformanceAnalyzer(
        warning_threshold=args.warning_threshold,
        critical_threshold=args.critical_threshold
    )

    # Загружаем результаты
    print(f"📊 Loading baseline from: {args.baseline}")
    baseline_results = analyzer.load_benchmark_results(args.baseline)

    print(f"📊 Loading current results from: {args.current}")
    current_results = analyzer.load_benchmark_results(args.current)

    if not baseline_results:
        print("❌ No baseline results loaded")
        sys.exit(1)

    if not current_results:
        print("❌ No current results loaded")
        sys.exit(1)

    print(f"✅ Loaded {len(baseline_results)} baseline and {len(current_results)} current results")

    # Выполняем сравнение
    comparisons = analyzer.compare_results(baseline_results, current_results)

    if not comparisons:
        print("⚠️ No common benchmarks found for comparison")
        sys.exit(0)

    # Генерируем отчет
    report = analyzer.generate_report(comparisons, args.format)

    # Выводим результат
    if args.output:
        with open(args.output, 'w', encoding='utf-8') as f:
            f.write(report)
        print(f"✅ Report saved to: {args.output}")
    else:
        print(report)

    # Проверяем на критические регрессии
    critical_count = len([c for c in comparisons if c.severity == 'critical'])

    if args.fail_on_regression and critical_count > 0:
        print(f"\\n🚨 {critical_count} critical performance regressions detected!")
        sys.exit(1)
    elif critical_count > 0:
        print(f"\\n⚠️ Warning: {critical_count} critical performance regressions detected")

    print(f"\\n✅ Performance comparison completed successfully")

if __name__ == '__main__':
    main()