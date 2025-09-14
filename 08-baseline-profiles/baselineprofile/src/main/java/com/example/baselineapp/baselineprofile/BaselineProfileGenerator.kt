package com.example.baselineapp.baselineprofile

import android.util.Log
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun generateBaselineProfile() {
        baselineProfileRule.collect(
            packageName = "com.example.baselineapp"
        ) {
            // Запуск приложения и навигация по ключевым экранам
            pressHome()
            startActivityAndWait()

            // Эмуляция пользовательских сценариев
            performAppStartupScenario()
            performMainNavigationScenario()
            performDataLoadingScenario()
        }
    }

    private fun performAppStartupScenario() {
        // Сценарий запуска приложения
        // Ожидание загрузки главного экрана
        device.waitForIdle(2000)

        // Взаимодействие с основными UI элементами нашего приложения
        // Ожидание загрузки списка элементов
        device.waitForWindowUpdate(null, 1000)

        // Ищем заголовок приложения
        device.findObject(By.text("Baseline Profile Demo"))?.let {
            Log.d("BaselineProfileGenerator", "Found app title")
        }
    }

    private fun performMainNavigationScenario() {
        // Навигация по основным разделам приложения
        device.waitForIdle(1000)

        // Тестируем навигацию по списку элементов
        repeat(3) { index ->
            // Ищем элементы списка и кликаем на них
            val listItems = device.findObjects(By.clickable(true))
            if (listItems.isNotEmpty() && index < listItems.size) {
                listItems[index].click()
                device.waitForIdle(1000)

                // Возвращаемся назад
                device.pressBack()
                device.waitForIdle(500)
            }
        }
    }

    private fun performDataLoadingScenario() {
        // Сценарий загрузки данных
        device.waitForIdle(1000)

        // Эмуляция скроллинга списков
        repeat(5) {
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight * 3 / 4,
                device.displayWidth / 2,
                device.displayHeight / 4,
                20
            )
            device.waitForIdle(300)
        }
    }

    @Test
    fun generateComprehensiveBaselineProfile() {
        baselineProfileRule.collect(
            packageName = "com.example.baselineapp",
            // Увеличиваем количество итераций для более точного профиля
            iterations = 3
        ) {
            pressHome()
            startActivityAndWait()

            // Комплексный сценарий использования
            performColdStartScenario()
            performHotPathScenarios()
            performMemoryIntensiveScenarios()
        }
    }

    private fun performColdStartScenario() {
        // Сценарий холодного старта
        device.waitForIdle(3000) // Даем время на полную загрузку

        // Взаимодействие с UI после полной загрузки
        // Это поможет захватить профиль инициализации
    }

    private fun performHotPathScenarios() {
        // Часто используемые пути в приложении
        repeat(10) { iteration ->
            // Симуляция частых пользовательских действий
            device.waitForIdle(200)

            // Например, поиск
            // device.findObject(By.res("search_button")).click()
            // device.findObject(By.res("search_input")).text = "query $iteration"

            device.waitForIdle(300)
            device.pressBack()
        }
    }

    private fun performMemoryIntensiveScenarios() {
        // Сценарии, которые активно используют память
        repeat(20) {
            // Прокрутка больших списков
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight * 4 / 5,
                device.displayWidth / 2,
                device.displayHeight / 5,
                15
            )
            device.waitForIdle(100)
        }

        // Возврат в начало списка
        repeat(20) {
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight / 5,
                device.displayWidth / 2,
                device.displayHeight * 4 / 5,
                15
            )
            device.waitForIdle(100)
        }
    }
}