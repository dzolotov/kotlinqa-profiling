package com.example.baseline

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Генератор Baseline Profiles для оптимизации производительности приложения
 *
 * Baseline Profiles указывают Android Runtime какие методы и классы
 * должны быть скомпилированы AOT (Ahead-of-Time) для улучшения производительности
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    /**
     * Генерация полного Baseline Profile
     * Покрывает все критические пути выполнения приложения
     */
    @Test
    fun generateBaselineProfile() {
        baselineProfileRule.collect(
            packageName = "com.example.myapp",
            maxIterations = 3, // Несколько итераций для стабильности
            stableIterations = 2 // Минимум стабильных итераций
        ) {
            // 1. Холодный старт приложения
            performColdStartup()

            // 2. Навигация по основным экранам
            performMainNavigation()

            // 3. Критические пользовательские сценарии
            performUserJourneys()

            // 4. Работа со списками и прокруткой
            performListInteractions()

            // 5. Поиск и фильтрация
            performSearchScenarios()

            // 6. Загрузка и кэширование данных
            performDataOperations()
        }
    }

    /**
     * Профиль для холодного старта
     * Оптимизирует время запуска приложения
     */
    @Test
    fun generateStartupProfile() {
        baselineProfileRule.collect(
            packageName = "com.example.myapp",
            profileBlock = {
                // Холодный старт с главного экрана
                pressHome()
                startActivityAndWait()

                // Ждем полной загрузки UI
                device.wait(Until.hasObject(By.text("Welcome")), 5000)

                // Инициализация критических компонентов
                device.wait(Until.hasObject(By.res("main_content")), 3000)
            }
        )
    }

    /**
     * Профиль для критических user journeys
     * Оптимизирует наиболее частые сценарии использования
     */
    @Test
    fun generateCriticalJourneysProfile() {
        baselineProfileRule.collect(
            packageName = "com.example.myapp"
        ) {
            startActivityAndWait()

            // Сценарий 1: Просмотр детальной информации
            performViewDetailsJourney()

            // Сценарий 2: Добавление в избранное
            performAddToFavoritesJourney()

            // Сценарий 3: Оформление заказа
            performCheckoutJourney()
        }
    }

    /**
     * Выполнение холодного старта приложения
     */
    private fun performColdStartup() {
        // Закрываем приложение если оно открыто
        device.pressHome()
        device.pressRecentApps()

        // Свайп для закрытия всех приложений
        val recent = device.findObject(By.res("com.android.systemui:id/recent_apps"))
        recent?.swipe(Direction.UP, 1.0f)

        // Запускаем приложение
        startActivityAndWait()

        // Ждем загрузки главного экрана
        device.wait(Until.hasObject(By.res("main_screen")), 5000)
    }

    /**
     * Навигация по основным экранам приложения
     */
    private fun performMainNavigation() {
        // Переход на экран каталога
        device.findObject(By.text("Catalog"))?.click()
        device.wait(Until.hasObject(By.res("catalog_list")), 3000)

        // Переход на экран профиля
        device.findObject(By.text("Profile"))?.click()
        device.wait(Until.hasObject(By.res("profile_info")), 3000)

        // Переход на экран настроек
        device.findObject(By.text("Settings"))?.click()
        device.wait(Until.hasObject(By.res("settings_list")), 3000)

        // Возврат на главный экран
        device.findObject(By.text("Home"))?.click()
        device.wait(Until.hasObject(By.res("main_screen")), 3000)
    }

    /**
     * Критические пользовательские сценарии
     */
    private fun performUserJourneys() {
        // Авторизация пользователя
        performLoginJourney()

        // Просмотр популярных товаров
        performBrowsePopularItems()

        // Работа с корзиной
        performCartOperations()
    }

    /**
     * Сценарий авторизации
     */
    private fun performLoginJourney() {
        // Переход на экран входа
        device.findObject(By.text("Sign In"))?.click()
        device.wait(Until.hasObject(By.res("login_form")), 3000)

        // Ввод данных
        device.findObject(By.res("email_input"))?.text = "test@example.com"
        device.findObject(By.res("password_input"))?.text = "password123"

        // Вход
        device.findObject(By.res("login_button"))?.click()
        device.wait(Until.hasObject(By.res("main_screen")), 5000)
    }

    /**
     * Работа со списками и прокруткой
     */
    private fun performListInteractions() {
        // Переход к списку
        device.findObject(By.text("Browse All"))?.click()
        device.wait(Until.hasObject(By.res("items_list")), 3000)

        // Прокрутка списка вниз
        val list = device.findObject(By.res("items_list"))
        repeat(5) {
            list?.scroll(Direction.DOWN, 0.8f)
            device.waitForIdle()
        }

        // Прокрутка списка вверх
        repeat(5) {
            list?.scroll(Direction.UP, 0.8f)
            device.waitForIdle()
        }

        // Клик на элемент списка
        device.findObject(By.res("item_card"))?.click()
        device.wait(Until.hasObject(By.res("item_details")), 3000)

        // Возврат к списку
        device.pressBack()
    }

    /**
     * Сценарии поиска
     */
    private fun performSearchScenarios() {
        // Открытие поиска
        device.findObject(By.res("search_button"))?.click()
        device.wait(Until.hasObject(By.res("search_input")), 2000)

        // Ввод поискового запроса
        val searchInput = device.findObject(By.res("search_input"))
        searchInput?.text = "test query"

        // Ожидание результатов
        device.wait(Until.hasObject(By.res("search_results")), 3000)

        // Применение фильтров
        device.findObject(By.text("Filters"))?.click()
        device.wait(Until.hasObject(By.res("filter_panel")), 2000)

        device.findObject(By.text("Price: Low to High"))?.click()
        device.findObject(By.text("Apply"))?.click()

        // Очистка поиска
        searchInput?.clear()
        device.pressBack()
    }

    /**
     * Операции с данными
     */
    private fun performDataOperations() {
        // Обновление данных (pull-to-refresh)
        val content = device.findObject(By.res("refreshable_content"))
        content?.swipe(Direction.DOWN, 0.8f)
        device.wait(Until.hasObject(By.res("loading_indicator")), 1000)
        device.wait(Until.gone(By.res("loading_indicator")), 5000)

        // Загрузка изображений
        device.findObject(By.text("Gallery"))?.click()
        device.wait(Until.hasObject(By.res("image_grid")), 3000)

        // Прокрутка галереи для загрузки изображений
        val gallery = device.findObject(By.res("image_grid"))
        repeat(3) {
            gallery?.scroll(Direction.DOWN, 0.5f)
            device.waitForIdle()
        }
    }

    /**
     * Сценарий просмотра деталей
     */
    private fun performViewDetailsJourney() {
        // Клик на элемент
        device.findObject(By.res("featured_item"))?.click()
        device.wait(Until.hasObject(By.res("item_details")), 3000)

        // Просмотр изображений
        device.findObject(By.res("image_pager"))?.swipe(Direction.LEFT, 0.5f)
        device.waitForIdle()

        // Чтение описания
        val description = device.findObject(By.res("description_text"))
        description?.scroll(Direction.DOWN, 0.3f)

        // Просмотр отзывов
        device.findObject(By.text("Reviews"))?.click()
        device.wait(Until.hasObject(By.res("reviews_list")), 2000)

        device.pressBack()
    }

    /**
     * Сценарий добавления в избранное
     */
    private fun performAddToFavoritesJourney() {
        // Поиск элемента
        val item = device.findObject(By.res("item_card"))
        item?.click()
        device.wait(Until.hasObject(By.res("item_details")), 3000)

        // Добавление в избранное
        device.findObject(By.res("favorite_button"))?.click()
        device.waitForIdle()

        // Переход в избранное
        device.pressBack()
        device.findObject(By.text("Favorites"))?.click()
        device.wait(Until.hasObject(By.res("favorites_list")), 3000)

        // Проверка наличия элемента
        device.wait(Until.hasObject(By.text("Added to favorites")), 2000)
    }

    /**
     * Сценарий оформления заказа
     */
    private fun performCheckoutJourney() {
        // Добавление в корзину
        device.findObject(By.res("add_to_cart_button"))?.click()
        device.waitForIdle()

        // Переход в корзину
        device.findObject(By.res("cart_icon"))?.click()
        device.wait(Until.hasObject(By.res("cart_items")), 3000)

        // Изменение количества
        device.findObject(By.res("quantity_plus"))?.click()
        device.waitForIdle()

        // Переход к оформлению
        device.findObject(By.text("Checkout"))?.click()
        device.wait(Until.hasObject(By.res("checkout_form")), 3000)

        // Заполнение формы
        device.findObject(By.res("address_input"))?.text = "123 Test Street"
        device.findObject(By.res("phone_input"))?.text = "+1234567890"

        // Выбор способа оплаты
        device.findObject(By.text("Payment Method"))?.click()
        device.findObject(By.text("Credit Card"))?.click()

        // Подтверждение заказа
        device.findObject(By.text("Place Order"))?.click()
        device.wait(Until.hasObject(By.text("Order Confirmed")), 5000)
    }

    /**
     * Просмотр популярных товаров
     */
    private fun performBrowsePopularItems() {
        // Переход к популярным
        device.findObject(By.text("Popular"))?.click()
        device.wait(Until.hasObject(By.res("popular_list")), 3000)

        // Просмотр нескольких товаров
        repeat(3) { index ->
            val items = device.findObjects(By.res("item_card"))
            if (items.size > index) {
                items[index].click()
                device.wait(Until.hasObject(By.res("item_details")), 2000)
                device.pressBack()
                device.waitForIdle()
            }
        }
    }

    /**
     * Операции с корзиной
     */
    private fun performCartOperations() {
        // Добавление нескольких товаров
        repeat(2) {
            device.findObject(By.res("item_card"))?.click()
            device.wait(Until.hasObject(By.res("add_to_cart_button")), 2000)
            device.findObject(By.res("add_to_cart_button"))?.click()
            device.pressBack()
        }

        // Переход в корзину
        device.findObject(By.res("cart_icon"))?.click()
        device.wait(Until.hasObject(By.res("cart_items")), 3000)

        // Удаление товара
        val deleteButton = device.findObject(By.res("delete_item"))
        deleteButton?.click()
        device.waitForIdle()

        // Очистка корзины
        device.findObject(By.text("Clear Cart"))?.click()
        device.findObject(By.text("Confirm"))?.click()
        device.waitForIdle()
    }
}