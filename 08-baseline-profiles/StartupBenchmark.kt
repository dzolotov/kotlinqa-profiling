package com.example.baseline

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkRule
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Макробенчмарки для измерения производительности приложения
 *
 * Сравнивает производительность:
 * - С Baseline Profile и без него
 * - Различные режимы компиляции
 * - Холодный/теплый/горячий запуск
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /**
     * Бенчмарк холодного запуска БЕЗ Baseline Profile
     * Baseline: измерение без оптимизаций
     */
    @Test
    fun startupNoCompilation() = startup(CompilationMode.None())

    /**
     * Бенчмарк холодного запуска С Baseline Profile
     * Показывает улучшение производительности на ~30%
     */
    @Test
    fun startupWithBaselineProfile() = startup(CompilationMode.Partial())

    /**
     * Бенчмарк с полной AOT компиляцией
     * Максимальная производительность, но увеличенный размер APK
     */
    @Test
    fun startupFullyCompiled() = startup(CompilationMode.Full())

    /**
     * Основной метод для измерения запуска
     */
    private fun startup(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(
                StartupTimingMetric(), // Метрики времени запуска
                FrameTimingMetric()    // Метрики отрисовки кадров
            ),
            iterations = 10, // Количество итераций для усреднения
            startupMode = StartupMode.COLD, // Холодный запуск
            compilationMode = compilationMode
        ) {
            // Сценарий запуска
            pressHome()
            startActivityAndWait()

            // Ждем полной загрузки UI
            device.wait(Until.hasObject(By.res("main_content")), 5000)
        }
    }

    /**
     * Бенчмарк теплого запуска
     * Приложение уже в памяти, но Activity пересоздается
     */
    @Test
    fun warmStartupBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.WARM,
            compilationMode = CompilationMode.Partial()
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    /**
     * Бенчмарк горячего запуска
     * Приложение возвращается из фона
     */
    @Test
    fun hotStartupBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.HOT,
            compilationMode = CompilationMode.Partial()
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    /**
     * Бенчмарк прокрутки списка
     * Измеряет производительность отрисовки при прокрутке
     */
    @Test
    fun scrollBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(
                FrameTimingMetric() // Метрики jank и frame drops
            ),
            iterations = 5,
            compilationMode = CompilationMode.Partial(),
            setupBlock = {
                // Подготовка: переход к списку
                pressHome()
                startActivityAndWait()

                device.findObject(By.text("List"))?.click()
                device.wait(Until.hasObject(By.res("recycler_view")), 5000)
            }
        ) {
            // Измеряемое действие: прокрутка
            val list = device.findObject(By.res("recycler_view"))

            // Прокрутка вниз
            repeat(10) {
                list?.scroll(Direction.DOWN, 0.9f)
                device.waitForIdle(500)
            }

            // Прокрутка вверх
            repeat(10) {
                list?.scroll(Direction.UP, 0.9f)
                device.waitForIdle(500)
            }
        }
    }

    /**
     * Бенчмарк навигации между экранами
     * Измеряет время перехода и отрисовки
     */
    @Test
    fun navigationBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(
                FrameTimingMetric(),
                StartupTimingMetric()
            ),
            iterations = 5,
            compilationMode = CompilationMode.Partial(),
            setupBlock = {
                pressHome()
                startActivityAndWait()
            }
        ) {
            // Навигация по основным экранам
            navigateToScreen("Profile")
            navigateToScreen("Settings")
            navigateToScreen("Home")
            navigateToScreen("Catalog")
        }
    }

    /**
     * Бенчмарк загрузки данных
     * Измеряет производительность при работе с данными
     */
    @Test
    fun dataLoadingBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(
                FrameTimingMetric(),
                StartupTimingMetric()
            ),
            iterations = 5,
            compilationMode = CompilationMode.Partial(),
            setupBlock = {
                pressHome()
                startActivityAndWait()
            }
        ) {
            // Pull-to-refresh
            val content = device.findObject(By.res("refreshable_content"))
            content?.swipe(Direction.DOWN, 0.8f)

            // Ждем загрузки
            device.wait(Until.hasObject(By.res("loading_indicator")), 1000)
            device.wait(Until.gone(By.res("loading_indicator")), 10000)

            // Проверяем что данные загружены
            device.wait(Until.hasObject(By.text("Updated")), 2000)
        }
    }

    /**
     * Бенчмарк поиска
     * Измеряет производительность поиска и фильтрации
     */
    @Test
    fun searchBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(
                FrameTimingMetric(),
                StartupTimingMetric()
            ),
            iterations = 5,
            compilationMode = CompilationMode.Partial(),
            setupBlock = {
                pressHome()
                startActivityAndWait()

                // Открываем поиск
                device.findObject(By.res("search_button"))?.click()
                device.wait(Until.hasObject(By.res("search_input")), 2000)
            }
        ) {
            val searchInput = device.findObject(By.res("search_input"))

            // Вводим запрос
            searchInput?.text = "test query"

            // Ждем результатов
            device.wait(Until.hasObject(By.res("search_results")), 5000)

            // Очищаем для следующей итерации
            searchInput?.clear()
            device.waitForIdle()
        }
    }

    /**
     * Бенчмарк сложных анимаций
     * Измеряет производительность при анимациях
     */
    @Test
    fun animationBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(
                FrameTimingMetric() // Особенно важно для анимаций
            ),
            iterations = 5,
            compilationMode = CompilationMode.Partial(),
            setupBlock = {
                pressHome()
                startActivityAndWait()

                // Переход к экрану с анимациями
                device.findObject(By.text("Animations"))?.click()
                device.wait(Until.hasObject(By.res("animation_view")), 3000)
            }
        ) {
            // Запуск анимации
            device.findObject(By.res("start_animation"))?.click()

            // Ждем завершения анимации
            device.wait(Until.hasObject(By.text("Animation Complete")), 5000)

            // Сброс для следующей итерации
            device.findObject(By.res("reset_animation"))?.click()
            device.waitForIdle()
        }
    }

    /**
     * Сравнительный бенчмарк с/без Baseline Profile
     * Запускает оба варианта и показывает разницу
     */
    @Test
    fun compareWithAndWithoutBaselineProfile() {
        val metrics = listOf(
            StartupTimingMetric(),
            FrameTimingMetric()
        )

        // Без Baseline Profile
        println("🔴 Running WITHOUT Baseline Profile...")
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = metrics,
            iterations = 5,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.None()
        ) {
            pressHome()
            startActivityAndWait()
            performComplexUserJourney()
        }

        // С Baseline Profile
        println("🟢 Running WITH Baseline Profile...")
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = metrics,
            iterations = 5,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            )
        ) {
            pressHome()
            startActivityAndWait()
            performComplexUserJourney()
        }
    }

    /**
     * Вспомогательный метод для навигации
     */
    private fun navigateToScreen(screenName: String) {
        device.findObject(By.text(screenName))?.click()
        device.wait(Until.hasObject(By.res("${screenName.lowercase()}_screen")), 3000)
        device.waitForIdle()
    }

    /**
     * Сложный пользовательский сценарий для тестирования
     */
    private fun performComplexUserJourney() {
        // Навигация
        navigateToScreen("Catalog")

        // Прокрутка списка
        val list = device.findObject(By.res("catalog_list"))
        list?.scroll(Direction.DOWN, 0.8f)

        // Клик на элемент
        device.findObject(By.res("item_card"))?.click()
        device.wait(Until.hasObject(By.res("item_details")), 3000)

        // Добавление в корзину
        device.findObject(By.res("add_to_cart"))?.click()
        device.waitForIdle()

        // Возврат
        device.pressBack()
    }
}