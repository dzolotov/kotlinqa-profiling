/*
 * TeamCity Configuration для Performance Testing
 * Kotlin DSL конфигурация для JetBrains TeamCity
 */

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.projectFeatures.buildReportTab
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.triggers.schedule

version = "2023.11"

project {
    description = "Kotlin Performance Testing Pipeline"

    buildType(KotlinJvmBenchmarks)
    buildType(AndroidPerformanceTests)
    buildType(JmhDeepAnalysis)
    buildType(PerformanceReportAggregator)

    // Настройка отчетов
    features {
        buildReportTab {
            title = "Performance Report"
            startPage = "performance-report.html"
        }
    }

    // Параметры проекта
    params {
        param("performance.threshold.warning", "120")  // 20% замедление
        param("performance.threshold.error", "200")    // 100% замедление
        param("kotlin.version", "1.9.20")
        param("android.compileSdk", "34")
    }
}

object KotlinJvmBenchmarks : BuildType({
    name = "Kotlin JVM Benchmarks"
    description = "Kotlin multiplatform performance benchmarks (JVM, JS, Native)"

    artifactRules = """
        build/reports/benchmarks => benchmarks-jvm.zip
        build/libs => jars.zip
    """.trimIndent()

    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }

    steps {
        gradle {
            name = "Build Project"
            tasks = "clean build -x test"
            gradleParams = "--info --stacktrace"
        }

        gradle {
            name = "Run JVM Benchmarks"
            tasks = "jvmBenchmark"
            gradleParams = "--info --continue"
        }

        gradle {
            name = "Run JS Benchmarks"
            tasks = "jsBenchmark"
            gradleParams = "--info --continue"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
        }

        gradle {
            name = "Run Native Benchmarks"
            tasks = "nativeBenchmark"
            gradleParams = "--info --continue"
            conditions {
                equals("teamcity.agent.jvm.os.name", "Linux")
            }
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
        }

        script {
            name = "Process Benchmark Results"
            scriptContent = """
                #!/bin/bash
                set -e

                echo "📊 Processing benchmark results..."

                # Создаем директорию для отчетов
                mkdir -p teamcity-reports

                # Обрабатываем JVM результаты
                if [ -d "build/reports/benchmarks/main" ]; then
                    echo "Processing JVM benchmarks..."
                    cp -r build/reports/benchmarks/main/* teamcity-reports/
                    echo "##teamcity[publishArtifacts 'teamcity-reports => jvm-benchmarks.zip']"
                fi

                # Обрабатываем JS результаты
                if [ -d "build/reports/benchmarks/js" ]; then
                    echo "Processing JS benchmarks..."
                    cp -r build/reports/benchmarks/js teamcity-reports/js-results
                    echo "##teamcity[publishArtifacts 'teamcity-reports/js-results => js-benchmarks.zip']"
                fi

                # Создаем сводную статистику
                echo "Generating summary statistics..."

                find teamcity-reports -name "*.json" -exec echo "Found benchmark result: {}" \;

                # TeamCity статистика
                if [ -f "teamcity-reports/jvm.json" ]; then
                    # Простой парсинг JSON для TeamCity метрик
                    python3 -c "
                import json
                import sys

                try:
                    with open('teamcity-reports/jvm.json', 'r') as f:
                        data = json.load(f)

                    for result in data:
                        benchmark_name = result['benchmark'].replace('.', '_')
                        score = result['primaryMetric']['score']

                        # Отправляем статистику в TeamCity
                        print(f'##teamcity[buildStatisticValue key=\'{benchmark_name}_score\' value=\'{score:.2f}\']')

                except Exception as e:
                    print(f'Error processing benchmark results: {e}')
                "
                fi

                echo "✅ Benchmark results processed successfully"
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            triggerRules = "+:root=*"
            branchFilter = "+:main +:develop +:feature/*"
        }
    }

    features {
        perfmon {
        }
    }

    requirements {
        moreThan("teamcity.agent.hardware.memorySizeMb", "4096")
    }

    // Параметры конфигурации
    params {
        param("env.GRADLE_OPTS", "-Dorg.gradle.daemon=false -Xmx2g")
        param("java.version", "17")
    }
})

object AndroidPerformanceTests : BuildType({
    name = "Android Performance Tests"
    description = "Android Jetpack Benchmark microbenchmarks and macrobenchmarks"

    artifactRules = """
        benchmark/build/outputs => android-micro-benchmarks.zip
        macrobenchmark/build/outputs => android-macro-benchmarks.zip
        benchmark-traces => benchmark-traces.zip
    """.trimIndent()

    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }

    steps {
        gradle {
            name = "Build Android App"
            tasks = "assembleRelease assembleAndroidTest assembleBenchmark"
            gradleParams = "--info"
        }

        script {
            name = "Setup Android Environment"
            scriptContent = """
                #!/bin/bash
                echo "Setting up Android environment..."

                # Настройки эмулятора для CI
                export ANDROID_SDK_ROOT=${'$'}ANDROID_HOME
                export PATH=${'$'}PATH:${'$'}ANDROID_HOME/tools:${'$'}ANDROID_HOME/platform-tools

                # Проверяем доступность ADB
                adb version

                # Проверяем подключенные устройства
                adb devices

                echo "Android environment ready"
            """.trimIndent()
        }

        gradle {
            name = "Run Microbenchmarks"
            tasks = "benchmark:connectedAndroidTest"
            gradleParams = """
                --info
                -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR,LOW-BATTERY,UNLOCKED
            """.trimIndent()
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
        }

        gradle {
            name = "Run Macrobenchmarks"
            tasks = "macrobenchmark:connectedAndroidTest"
            gradleParams = """
                --info
                -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR,LOW-BATTERY,UNLOCKED
            """.trimIndent()
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
        }

        script {
            name = "Extract Benchmark Traces"
            scriptContent = """
                #!/bin/bash
                set -e

                echo "📱 Extracting Android benchmark traces..."

                # Создаем директорию для trace файлов
                mkdir -p benchmark-traces

                # Проверяем наличие устройства
                if adb devices | grep -q "device${'$'}"; then
                    echo "Device found, extracting traces..."

                    # Извлекаем trace файлы
                    adb shell ls /sdcard/Android/media/*/additional_test_output/ || echo "No benchmark outputs found"

                    # Пытаемся извлечь файлы
                    adb pull /sdcard/Android/media/ benchmark-traces/ || echo "Could not pull benchmark files"

                    # Подсчитываем извлеченные файлы
                    trace_count=${'$'}(find benchmark-traces -name "*.perfetto-trace" | wc -l)
                    json_count=${'$'}(find benchmark-traces -name "*.json" | wc -l)

                    echo "Extracted ${'$'}trace_count Perfetto traces and ${'$'}json_count JSON files"

                    # TeamCity статистика
                    echo "##teamcity[buildStatisticValue key='android_perfetto_traces' value='${'$'}trace_count']"
                    echo "##teamcity[buildStatisticValue key='android_json_results' value='${'$'}json_count']"

                    # Показываем размеры файлов
                    if [ ${'$'}trace_count -gt 0 ]; then
                        echo "Perfetto trace file sizes:"
                        find benchmark-traces -name "*.perfetto-trace" -exec du -h {} \;
                    fi
                else
                    echo "❌ No Android device connected - skipping trace extraction"
                    echo "##teamcity[buildStatisticValue key='android_device_available' value='0']"
                fi

                echo "✅ Android benchmark trace extraction completed"
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            triggerRules = "+:root=*"
            branchFilter = "+:main +:develop"
        }

        schedule {
            schedulingPolicy = daily {
                hour = 2
                minute = 30
            }
            branchFilter = "+:main"
            triggerBuild = always()
        }
    }

    requirements {
        moreThan("teamcity.agent.hardware.memorySizeMb", "8192")
        contains("teamcity.agent.name", "android")  // Только агенты с Android SDK
    }

    // Параметры для Android
    params {
        param("env.ANDROID_HOME", "/opt/android-sdk")
        param("android.emulator.avd", "TeamCity_API_29")
    }
})

object JmhDeepAnalysis : BuildType({
    name = "JMH Deep Analysis"
    description = "Подробный анализ производительности с JMH"

    artifactRules = """
        build/libs/jmh.jar => jmh-jar.zip
        jmh-results.json => jmh-results.zip
        jmh-report.html => jmh-report.zip
    """.trimIndent()

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            name = "Build JMH JAR"
            tasks = "jmhJar"
            gradleParams = "--info"
        }

        script {
            name = "Run JMH Benchmarks"
            scriptContent = """
                #!/bin/bash
                set -e

                echo "⚡ Running JMH deep performance analysis..."

                # Проверяем наличие JMH jar
                if [ ! -f "build/libs/jmh.jar" ]; then
                    echo "❌ JMH jar not found!"
                    exit 1
                fi

                # Запуск JMH с детальной конфигурацией
                java -jar build/libs/jmh.jar \
                    -wi 3 \
                    -i 5 \
                    -f 1 \
                    -r 1s \
                    -w 1s \
                    -rf json \
                    -rff jmh-results.json \
                    -v EXTRA

                echo "✅ JMH benchmarks completed"

                # Обработка результатов
                if [ -f "jmh-results.json" ]; then
                    echo "Processing JMH results..."

                    # Создание человекочитаемого отчета
                    python3 -c "
                import json
                import sys

                try:
                    with open('jmh-results.json', 'r') as f:
                        results = json.load(f)

                    print('JMH Benchmark Results Summary:')
                    print('=' * 50)

                    total_benchmarks = len(results)
                    print(f'Total benchmarks: {total_benchmarks}')

                    # TeamCity статистика
                    print(f'##teamcity[buildStatisticValue key=\\\"jmh_total_benchmarks\\\" value=\\\"{total_benchmarks}\\\"]')

                    for result in results:
                        benchmark = result['benchmark']
                        score = result['primaryMetric']['score']
                        unit = result['primaryMetric']['scoreUnit']
                        error = result['primaryMetric']['scoreError']

                        print(f'{benchmark}: {score:.2f} ± {error:.2f} {unit}')

                        # TeamCity статистика для каждого бенчмарка
                        safe_name = benchmark.replace('.', '_').replace(':', '_')
                        print(f'##teamcity[buildStatisticValue key=\\\"jmh_{safe_name}_score\\\" value=\\\"{score:.2f}\\\"]')
                        print(f'##teamcity[buildStatisticValue key=\\\"jmh_{safe_name}_error\\\" value=\\\"{error:.2f}\\\"]')

                except Exception as e:
                    print(f'Error processing JMH results: {e}')
                    sys.exit(1)
                "

                    echo "JMH results processed successfully"
                else
                    echo "❌ No JMH results found"
                    exit 1
                fi
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            triggerRules = "+:root=*"
            branchFilter = "+:main"
        }
    }

    requirements {
        moreThan("teamcity.agent.hardware.memorySizeMb", "4096")
        moreThan("teamcity.agent.hardware.cpuCount", "2")
    }

    params {
        param("env.JMH_OPTS", "-Xmx2g")
    }
})

object PerformanceReportAggregator : BuildType({
    name = "Performance Report Aggregator"
    description = "Сводный отчет по всем performance тестам"

    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        root(DslContext.settingsRoot)
        showDependenciesChanges = true
    }

    dependencies {
        dependency(KotlinJvmBenchmarks) {
            snapshot {
                onDependencyFailure = FailureAction.IGNORE
            }
            artifacts {
                buildRule = lastSuccessful()
                artifactRules = "benchmarks-jvm.zip => artifacts/"
            }
        }

        dependency(AndroidPerformanceTests) {
            snapshot {
                onDependencyFailure = FailureAction.IGNORE
            }
            artifacts {
                buildRule = lastSuccessful()
                artifactRules = """
                    android-micro-benchmarks.zip => artifacts/
                    android-macro-benchmarks.zip => artifacts/
                    benchmark-traces.zip => artifacts/
                """.trimIndent()
            }
        }

        dependency(JmhDeepAnalysis) {
            snapshot {
                onDependencyFailure = FailureAction.IGNORE
            }
            artifacts {
                buildRule = lastSuccessful()
                artifactRules = "jmh-results.zip => artifacts/"
            }
        }
    }

    steps {
        script {
            name = "Generate Performance Report"
            scriptContent = """
                #!/bin/bash
                set -e

                echo "📊 Generating comprehensive performance report..."

                # Создаем директорию для отчета
                mkdir -p performance-report

                # Создаем HTML отчет
                cat > performance-report/index.html << 'EOF'
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Performance Test Report - Build %build.number%</title>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        .summary { background: #f5f5f5; padding: 15px; border-radius: 5px; }
                        .result { margin: 10px 0; padding: 10px; border-left: 4px solid #4CAF50; }
                        .warning { border-left-color: #FF9800; }
                        .error { border-left-color: #F44336; }
                        table { width: 100%; border-collapse: collapse; margin: 15px 0; }
                        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
                        th { background-color: #f2f2f2; }
                    </style>
                </head>
                <body>
                    <h1>🚀 Performance Test Report</h1>

                    <div class="summary">
                        <h2>Build Information</h2>
                        <ul>
                            <li><strong>Build Number:</strong> %build.number%</li>
                            <li><strong>Build VCS Number:</strong> %build.vcs.number%</li>
                            <li><strong>Agent:</strong> %agent.name%</li>
                            <li><strong>Date:</strong> %teamcity.build.start.date%</li>
                        </ul>
                    </div>

                    <h2>📋 Test Results Summary</h2>
                    <table>
                        <tr><th>Test Suite</th><th>Status</th><th>Artifacts</th></tr>
                EOF

                # Проверяем наличие артефактов и добавляем в отчет
                if [ -d "artifacts" ]; then
                    for artifact in artifacts/*.zip; do
                        if [ -f "$artifact" ]; then
                            basename=$(basename "$artifact" .zip)
                            echo "<tr><td>$basename</td><td>✅ Completed</td><td><a href=\"../artifacts/$basename.zip\">Download</a></td></tr>" >> performance-report/index.html
                        fi
                    done
                else
                    echo "<tr><td colspan=\"3\">❌ No artifacts found</td></tr>" >> performance-report/index.html
                fi

                cat >> performance-report/index.html << 'EOF'
                    </table>

                    <h2>🎯 Next Steps</h2>
                    <ol>
                        <li>Download and analyze benchmark artifacts</li>
                        <li>Compare results with previous builds</li>
                        <li>Investigate any performance regressions</li>
                        <li>Update baseline performance metrics if needed</li>
                    </ol>

                    <p><small>Report generated automatically by TeamCity Performance Pipeline</small></p>
                </body>
                </html>
                EOF

                echo "✅ Performance report generated successfully"
                echo "##teamcity[publishArtifacts 'performance-report => performance-report.zip']"
            """.trimIndent()
        }
    }

    triggers {
        finishBuildTrigger {
            buildType = "${KotlinJvmBenchmarks.id}"
            successfulOnly = false
        }
        finishBuildTrigger {
            buildType = "${AndroidPerformanceTests.id}"
            successfulOnly = false
        }
        finishBuildTrigger {
            buildType = "${JmhDeepAnalysis.id}"
            successfulOnly = false
        }
    }

    artifactRules = "performance-report.zip"
}

// Дополнительные утилиты для TeamCity

// Шаблон для бенчмарков
template {
    name = "Benchmark Template"

    params {
        param("benchmark.warmup.iterations", "3")
        param("benchmark.measurement.iterations", "5")
        param("benchmark.forks", "1")
    }

    steps {
        script {
            name = "Performance Check"
            scriptContent = """
                echo "Checking performance baseline..."
                # Здесь может быть логика сравнения с baseline
            """.trimIndent()
        }
    }
}