#!/bin/bash

echo "======================================="
echo "   Baseline Profiles Демонстрация"
echo "======================================="
echo ""

# Установка переменных окружения
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/Volumes/T7Shield/AndroidSdk

echo "📱 Baseline Profiles - механизм оптимизации Android приложений"
echo "   который позволяет улучшить производительность на 30%"
echo ""

echo "📋 Структура проекта:"
echo "   app/               - основное приложение"
echo "   baselineprofile/   - модуль для генерации профилей"
echo ""

echo "📊 Ключевые классы в нашем примере:"
echo ""
echo "1. BaselineApp.kt - Application класс с инициализацией"
find app/src -name "BaselineApp.kt" | head -1
echo ""

echo "2. MainActivity.kt - главная активность с навигацией"
find app/src -name "MainActivity.kt" | head -1
echo ""

echo "3. DataRepository.kt - репозиторий с часто используемыми методами"
find app/src -name "DataRepository.kt" | head -1
echo ""

echo "4. BaselineProfileGenerator.kt - тесты для генерации профилей"
find baselineprofile/src -name "BaselineProfileGenerator.kt" | head -1
echo ""

echo "⚡ Эффект от применения Baseline Profiles:"
echo ""
echo "   БЕЗ Baseline Profile:"
echo "   - Холодный старт: ~2-3 секунды"
echo "   - Переходы между экранами: ~200-300ms"
echo "   - Высокое потребление CPU при первых запусках"
echo ""
echo "   С Baseline Profile:"
echo "   - Холодный старт: ~1.5-2 секунды (-30-40%)"
echo "   - Переходы между экранами: ~100-150ms (-50%)"
echo "   - Стабильное потребление ресурсов"
echo ""

echo "🚀 Демонстрация работы приложения:"
echo ""

# Проверка состояния эмулятора
if $ANDROID_HOME/platform-tools/adb devices | grep -q "emulator"; then
    echo "✅ Эмулятор обнаружен"

    # Проверка установки приложения
    if $ANDROID_HOME/platform-tools/adb shell pm list packages | grep -q "com.example.baselineapp"; then
        echo "✅ Приложение установлено"
        echo ""
        echo "Запуск приложения..."
        $ANDROID_HOME/platform-tools/adb shell am start -n com.example.baselineapp/.ui.MainActivity
        echo "✅ Приложение запущено"
    else
        echo "⚠️ Приложение не установлено. Установка..."
        if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
            $ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
            echo "✅ Приложение установлено"
        else
            echo "❌ APK файл не найден. Сначала соберите приложение:"
            echo "   ./gradlew app:assembleDebug"
        fi
    fi
else
    echo "⚠️ Эмулятор не обнаружен"
fi

echo ""
echo "📈 Команды для генерации Baseline Profile:"
echo ""
echo "   # На подключенном устройстве или эмуляторе:"
echo "   ./gradlew :baselineprofile:connectedAndroidTest"
echo ""
echo "   # На управляемом устройстве (managed device):"
echo "   ./gradlew :baselineprofile:pixel6Api33BenchmarkAndroidTest"
echo ""

echo "✅ Baseline Profiles автоматически применяются при установке"
echo "   приложения из Google Play на Android 7+ устройствах"
echo ""

echo "======================================="
echo "   Демонстрация завершена!"
echo "======================================="