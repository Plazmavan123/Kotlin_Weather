package org.example.app

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatLightLaf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import org.example.utils.Printer
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import java.awt.Image
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import java.io.File
import java.awt.GridLayout
import javax.swing.JScrollPane

fun main() {
    FlatLightLaf.setup()
    val frame = JFrame("Приложение погода")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.size = Dimension(400, 400) // Увеличиваем высоту для истории
    frame.layout = BorderLayout()

    val historyFile = File("weather_history.txt")

    // Панель ввода
    val inputPanel = JPanel(FlowLayout())
    val cityField = JTextField(15)
    val searchButton = JButton("Узнать погоду")

    inputPanel.add(JLabel("Город"))
    inputPanel.add(cityField)
    inputPanel.add(searchButton)

    // Панель с информацией о погоде
    val weatherPanel = JPanel()
    weatherPanel.layout = BoxLayout(weatherPanel, BoxLayout.Y_AXIS)
    weatherPanel.border = BorderFactory.createEmptyBorder(10,10,10,10)

    val cityLabel = JLabel("")
    val tempLabel = JLabel("")
    val feelLikeLabel = JLabel("")
    val humidityLabel = JLabel("")
    val descriptionLabel = JLabel("")
    val weatherIcon = JLabel()

    weatherPanel.add(cityLabel)
    weatherPanel.add(tempLabel)
    weatherPanel.add(feelLikeLabel)
    weatherPanel.add(humidityLabel)
    weatherPanel.add(descriptionLabel)
    weatherPanel.add(weatherIcon)

    // Панель истории поиска
    val historyPanel = JPanel()
    historyPanel.layout = BoxLayout(historyPanel, BoxLayout.Y_AXIS)
    historyPanel.border = BorderFactory.createTitledBorder("Недавние запросы")
    val historyScrollPane = JScrollPane(historyPanel)
    historyScrollPane.preferredSize = Dimension(400, 100)

    // Функция для обновления истории
    fun updateHistoryPanel() {
        historyPanel.removeAll()
        if (historyFile.exists()) {
            val recentCities = historyFile.readLines().takeLast(6).reversed()
            for (city in recentCities) {
                val cityButton = JButton(city)
                cityButton.addActionListener {
                    cityField.text = city
                    // Автоматически выполняем поиск при клике
                    searchButton.doClick()
                }
                historyPanel.add(cityButton)
            }
        }
        historyPanel.revalidate()
        historyPanel.repaint()
    }

    // Функция для добавления города в историю
    fun addToHistory(city: String) {
        val allCities = if (historyFile.exists()) {
            historyFile.readLines().toMutableList()
        } else {
            mutableListOf()
        }

        // Удаляем город если он уже есть (чтобы не было дубликатов)
        allCities.remove(city)
        // Добавляем город в конец
        allCities.add(city)
        // Сохраняем только 6 последних
        val updatedCities = allCities.takeLast(6)

        historyFile.writeText(updatedCities.joinToString("\n"))
        updateHistoryPanel()
    }

    // Функция для получения погоды
    fun fetchWeather(city: String) {
        runBlocking {
            try {
                val weather = withContext(Dispatchers.IO) {
                    RetrofitInstance.weatherAPI.getCurrentWeather(city)
                }
                SwingUtilities.invokeLater {
                    cityLabel.text = "Город: ${weather.name}"
                    tempLabel.text = "Температура: ${weather.main.temp} C"
                    feelLikeLabel.text = "Ощущается как: ${weather.main.feels_like} C"
                    humidityLabel.text = "Влажность: ${weather.main.humidity} %"

                    if (weather.weather.isNotEmpty()) {
                        val weatherInfo = weather.weather[0]
                        descriptionLabel.text = "Описание: ${weatherInfo.description}"
                        val iconUrl = "https://openweathermap.org/img/wn/${weatherInfo.icon}@2x.png"
                        try {
                            val image = ImageIO.read(URL(iconUrl))
                            val scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH)
                            weatherIcon.icon = ImageIcon(scaledImage)
                        } catch (e: Exception) {
                            weatherIcon.icon = null
                        }
                    }
                    // Добавляем успешный запрос в историю
                    addToHistory(city)
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    JOptionPane.showMessageDialog(frame, "Ошибка: ${e.message}", "Ошибка!", JOptionPane.ERROR_MESSAGE)
                }
            }
        }
    }

    searchButton.addActionListener {
        val city = cityField.text.trim()
        if (city.isNotEmpty()) {
            fetchWeather(city)
        }
    }

    // Инициализируем панель истории при запуске
    updateHistoryPanel()

    frame.add(inputPanel, BorderLayout.NORTH)
    frame.add(weatherPanel, BorderLayout.CENTER)
    frame.add(historyScrollPane, BorderLayout.SOUTH)
    frame.isVisible = true
}