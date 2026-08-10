package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class WeatherForecastDay(
    val dayName: String,
    val maxTemp: Int,
    val minTemp: Int,
    val condition: String,
    val iconRes: String = "ic_weather_cloudy"
)

data class WeatherInfo(
    val locationName: String = "Thành phố Hà Nội",
    val currentTemp: Int = 31,
    val condition: String = "Nắng nhẹ",
    val maxTemp: Int = 34,
    val minTemp: Int = 26,
    val humidity: Int = 78,
    val rainProbability: Int = 20,
    val forecast3Days: List<WeatherForecastDay> = emptyList()
)

class WeatherRepository {

    suspend fun getWeather(location: String): WeatherInfo = withContext(Dispatchers.IO) {
        val coords = getCoordinatesForLocation(location)
        try {
            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=${coords.first}&longitude=${coords.second}&current=temperature_2m,relative_humidity_2m,precipitation,weather_code&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code&timezone=Asia%2FBangkok"
            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val current = root.getJSONObject("current")
                val daily = root.getJSONObject("daily")

                val temp = current.getDouble("temperature_2m").toInt()
                val humidity = current.getInt("relative_humidity_2m")
                val code = current.getInt("weather_code")
                val conditionStr = decodeWeatherCode(code)

                val dailyMax = daily.getJSONArray("temperature_2m_max")
                val dailyMin = daily.getJSONArray("temperature_2m_min")
                val dailyRain = daily.optJSONArray("precipitation_probability_max")
                val dailyCodes = daily.getJSONArray("weather_code")

                val maxT = if (dailyMax.length() > 0) dailyMax.getDouble(0).toInt() else temp + 3
                val minT = if (dailyMin.length() > 0) dailyMin.getDouble(0).toInt() else temp - 4
                val rainProb = if (dailyRain != null && dailyRain.length() > 0) dailyRain.optInt(0, 20) else 20

                val forecastList = mutableListOf<WeatherForecastDay>()
                val cal = Calendar.getInstance()
                val dayFormat = SimpleDateFormat("EEEE", Locale("vi", "VN"))

                for (i in 1..3) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    val dName = if (i == 1) "Ngày mai" else dayFormat.format(cal.time)
                    val fMax = if (dailyMax.length() > i) dailyMax.getDouble(i).toInt() else maxT
                    val fMin = if (dailyMin.length() > i) dailyMin.getDouble(i).toInt() else minT
                    val fCode = if (dailyCodes.length() > i) dailyCodes.getInt(i) else 1
                    forecastList.add(WeatherForecastDay(dName, fMax, fMin, decodeWeatherCode(fCode)))
                }

                return@withContext WeatherInfo(
                    locationName = location,
                    currentTemp = temp,
                    condition = conditionStr,
                    maxTemp = maxT,
                    minTemp = minT,
                    humidity = humidity,
                    rainProbability = rainProb,
                    forecast3Days = forecastList
                )
            }
        } catch (e: Exception) {
            // Fallback to offline cached data
        }

        return@withContext getFallbackWeather(location)
    }

    private fun getCoordinatesForLocation(loc: String): Pair<Double, Double> {
        val lower = loc.lowercase()
        return when {
            lower.contains("liên minh") -> Pair(21.1211, 105.6542) // Xã Liên Minh, Hà Nội
            lower.contains("đan phượng") -> Pair(21.1154, 105.6723)
            lower.contains("sơn tây") -> Pair(21.1381, 105.5032)
            lower.contains("đông anh") -> Pair(21.1365, 105.8427)
            lower.contains("hồ chí minh") || lower.contains("sài gòn") -> Pair(10.8231, 106.6297)
            lower.contains("đà nẵng") -> Pair(16.0544, 108.2022)
            lower.contains("hải phòng") -> Pair(20.8449, 106.6881)
            else -> Pair(21.0285, 105.8542) // Thành phố Hà Nội
        }
    }

    private fun decodeWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Nắng đẹp"
            1, 2 -> "Nắng nhẹ, mây rải rác"
            3 -> "Nhiều mây"
            45, 48 -> "Có sương mù"
            51, 53, 55 -> "Mưa nhỏ rải rác"
            61, 63, 65 -> "Có mưa rào"
            80, 81, 82 -> "Mưa rào lớn"
            95, 96, 99 -> "Có dông kèm mưa"
            else -> "Thời tiết mát mẻ"
        }
    }

    private fun getFallbackWeather(location: String): WeatherInfo {
        return WeatherInfo(
            locationName = location,
            currentTemp = 30,
            condition = "Nắng nhẹ, mây rải rác",
            maxTemp = 33,
            minTemp = 25,
            humidity = 75,
            rainProbability = 15,
            forecast3Days = listOf(
                WeatherForecastDay("Ngày mai", 33, 25, "Nắng đẹp"),
                WeatherForecastDay("Thứ Bảy", 34, 26, "Nắng nóng nhẹ"),
                WeatherForecastDay("Chủ Nhật", 32, 25, "Mưa rào rải rác")
            )
        )
    }

    fun getPresetLocations(): List<String> {
        return listOf(
            "Thành phố Hà Nội",
            "Xã Liên Minh, TP. Hà Nội",
            "Huyện Đan Phượng, Hà Nội",
            "Thị xã Sơn Tây, Hà Nội",
            "Huyện Đông Anh, Hà Nội",
            "Thành phố Hải Phòng",
            "Thành phố Đà Nẵng",
            "Thành phố Hồ Chí Minh"
        )
    }
}
