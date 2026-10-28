package com.app.havadurumu

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var tvCity: TextView
    private lateinit var tvBigTemp: TextView
    private lateinit var tvBigIcon: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvFeelsAndRange: TextView
    private lateinit var tvAlertBanner: TextView
    private lateinit var hourlyContainer: LinearLayout
    private lateinit var dailyContainer: LinearLayout
    private lateinit var tvMetricWind: TextView
    private lateinit var tvMetricHum: TextView
    private lateinit var tvMetricUv: TextView
    private lateinit var tvMetricPress: TextView

    private var openDayContainer: LinearLayout? = null
    private var openDayArrowView: TextView? = null
    private var openDayBaseText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        window.statusBarColor = Color.parseColor("#081026")

        val prefs = getSharedPreferences("SamsungOneUiWeather", Context.MODE_PRIVATE)

        val bgGradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#081026"), Color.parseColor("#11224D"), Color.parseColor("#091226"))
        )

        val scroll = ScrollView(this).apply {
            background = bgGradient
            isFillViewport = true
            fitsSystemWindows = true
        }

        val topSpace = getStatusBarHeight() + dp(18)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), topSpace, dp(16), dp(32))
        }

        // 1. ARAMA ÇUBUĞU
        val searchBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = cardStyle("#192B52", "#2B4680", 28f)
            setPadding(dp(18), dp(6), dp(8), dp(6))
            gravity = Gravity.CENTER_VERTICAL
        }
        val tvSearchIcon = label("🔍", 15f, "#93C5FD", false).apply {
            setPadding(0, 0, dp(10), 0)
        }
        val etCity = EditText(this).apply {
            hint = "Şehir veya ilçe ara..."
            setHintTextColor(Color.parseColor("#8DA2C0"))
            setTextColor(Color.WHITE)
            textSize = 14.5f
            background = null
            isSingleLine = true
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            layoutParams = LinearLayout.LayoutParams(0, dp(42), 1f)
        }
        val btnSearch = Button(this).apply {
            text = "Bul"
            textSize = 13f
            setTextColor(Color.WHITE)
            background = cardStyle("#2563EB", "#3B82F6", 22f)
            layoutParams = LinearLayout.LayoutParams(dp(68), dp(38))
            setPadding(0, 0, 0, 0)
        }
        searchBar.addView(tvSearchIcon)
        searchBar.addView(etCity)
        searchBar.addView(btnSearch)
        root.addView(searchBar)

        // 2. ANA BAŞLIK ALANI
        val heroBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(8), dp(24), dp(8), dp(18))
        }
        // Eğer daha önce aratılmış bir şehir yoksa "Şehir Seçin" yazar
        tvCity = label(prefs.getString("city", "Şehir Seçin")!!, 26f, "#FFFFFF", true)
        heroBox.addView(tvCity)

        val tempRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(4))
        }
        tvBigIcon = label(prefs.getString("icon", "🌍")!!, 52f, "#FFFFFF", false).apply {
            setPadding(0, 0, dp(12), 0)
        }
        tvBigTemp = label(prefs.getString("temp", "--°")!!, 66f, "#FFFFFF", false)
        tempRow.addView(tvBigIcon)
        tempRow.addView(tvBigTemp)
        heroBox.addView(tempRow)

        tvCondition = label(prefs.getString("cond", "Yukarıdan bir şehir aratın")!!, 19f, "#93C5FD", true)
        tvFeelsAndRange = label(
            prefs.getString("sub", "Hava durumu verisi için arama yapın")!!,
            13.5f, "#CBD5E1", false
        ).apply { setPadding(0, dp(6), 0, 0) }

        heroBox.addView(tvCondition)
        heroBox.addView(tvFeelsAndRange)
        root.addView(heroBox)

        // 3. BİLGİLENDİRME KUTUSU
        tvAlertBanner = label(prefs.getString("alert", "💡 İstediğiniz şehri arama çubuğundan bulabilirsiniz.")!!, 13.5f, "#FDE047", false).apply {
            background = cardStyle("#192B52", "#2B4680", 22f)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(16) }
        }
        root.addView(tvAlertBanner)

        // 4. SAATLİK AKIŞ
        root.addView(sectionHeader("🕒 Saatlik Tahmin"))
        val hourlyScroll = HorizontalScrollView(this).apply {
            background = cardStyle("#192B52", "#2B4680", 24f)
            isHorizontalScrollBarEnabled = false
            setPadding(dp(10), dp(14), dp(10), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(18) }
        }
        hourlyContainer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        hourlyScroll.addView(hourlyContainer)
        root.addView(hourlyScroll)

        // 5. 7 GÜNLÜK TAHMİN
        root.addView(sectionHeader("📅 7 Günlük Tahmin"))
        dailyContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(18) }
        }
        root.addView(dailyContainer)

        // 6. ALT DETAY KARTLARI
        root.addView(sectionHeader("🧭 Ölçümler"))
        val gridRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
        }
        val gridRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        }

        tvMetricWind = metricBox("💨 Rüzgar", "-- km/s", true)
        tvMetricHum = metricBox("💧 Nem", "%--", false)
        tvMetricUv = metricBox("☀️ UV İndeksi", "--", true)
        tvMetricPress = metricBox("⏲️ Basınç", "-- hPa", false)

        gridRow1.addView(tvMetricWind)
        gridRow1.addView(tvMetricHum)
        gridRow2.addView(tvMetricUv)
        gridRow2.addView(tvMetricPress)
        root.addView(gridRow1)
        root.addView(gridRow2)

        scroll.addView(root)
        setContentView(scroll)

        // Eğer hafızada kayıtlı son şehir varsa onu yükle, yoksa boş bırak
        val lastCity = prefs.getString("city", null)
        if (lastCity != null) {
            fetchWeather(lastCity)
        }

        val doSearch = {
            val q = etCity.text.toString().trim()
            if (q.isNotEmpty()) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etCity.windowToken, 0)
                fetchWeather(q)
                etCity.setText("")
            }
        }
        btnSearch.setOnClickListener { doSearch() }
        etCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doSearch(); true } else false
        }
    }

    private fun fetchWeather(cityName: String) {
        executor.execute {
            try {
                val enc = URLEncoder.encode(cityName, "UTF-8")
                val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$enc&count=1&language=tr&format=json"
                val geoJson = JSONObject(httpGet(geoUrl))
                if (!geoJson.has("results")) {
                    runOnUiThread {
                        Toast.makeText(this, "'$cityName' bulunamadı!", Toast.LENGTH_SHORT).show()
                    }
                    return@execute
                }

                val loc = geoJson.getJSONArray("results").getJSONObject(0)
                val lat = loc.getDouble("latitude")
                val lon = loc.getDouble("longitude")
                val resolvedName = loc.getString("name")

                val wUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                        "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,surface_pressure" +
                        "&hourly=temperature_2m,precipitation_probability,weather_code" +
                        "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,precipitation_sum,wind_speed_10m_max,uv_index_max,sunrise,sunset" +
                        "&forecast_days=7&timezone=auto"

                val wJson = JSONObject(httpGet(wUrl))

                val cur = wJson.getJSONObject("current")
                val curTemp = rInt(cur.getDouble("temperature_2m"))
                val curFeel = rInt(cur.getDouble("apparent_temperature"))
                val curHum = cur.getInt("relative_humidity_2m")
                val curWind = rInt(cur.getDouble("wind_speed_10m"))
                val curPress = rInt(cur.optDouble("surface_pressure", 1013.0))
                val curCode = cur.getInt("weather_code")
                val curIcon = getIcon(curCode)
                val curDesc = getDesc(curCode)

                val daily = wJson.getJSONObject("daily")
                val dates = daily.getJSONArray("time")
                val maxs = daily.getJSONArray("temperature_2m_max")
                val mins = daily.getJSONArray("temperature_2m_min")
                val dCodes = daily.getJSONArray("weather_code")
                val rainProbs = daily.optJSONArray("precipitation_probability_max")
                val rainSums = daily.optJSONArray("precipitation_sum")
                val maxWinds = daily.optJSONArray("wind_speed_10m_max")
                val uvMaxs = daily.optJSONArray("uv_index_max")
                val sunrises = daily.optJSONArray("sunrise")
                val sunsets = daily.optJSONArray("sunset")

                val hourly = wJson.getJSONObject("hourly")
                val hTimes = hourly.getJSONArray("time")
                val hTemps = hourly.getJSONArray("temperature_2m")
                val hProbs = hourly.optJSONArray("precipitation_probability")
                val hCodes = hourly.getJSONArray("weather_code")

                val todayMax = rInt(maxs.getDouble(0))
                val todayMin = rInt(mins.getDouble(0))
                val todayUv = if (uvMaxs != null && !uvMaxs.isNull(0)) String.format(Locale.US, "%.1f", uvMaxs.getDouble(0)) else "0"
                val todayRainProb = if (rainProbs != null && !rainProbs.isNull(0)) rainProbs.getInt(0) else 0

                var rainyCount = 0
                for (i in 0 until minOf(7, dates.length())) {
                    val rp = if (rainProbs != null && !rainProbs.isNull(i)) rainProbs.getInt(i) else 0
                    if (rp >= 50 || dCodes.getInt(i) >= 51) rainyCount++
                }

                val alertText = buildString {
                    if (todayRainProb >= 50 || curCode >= 51) {
                        append("⚠️ Bugün yağış bekleniyor (%$todayRainProb ihtimal). Şemsiye bulundurun.\n")
                    } else if (curWind >= 30) {
                        append("💨 Bugün rüzgar kuvvetli esiyor ($curWind km/s).\n")
                    } else {
                        append("✅ Bugün yağış beklenmiyor, hava $curDesc.\n")
                    }
                    append("📅 Önümüzdeki 7 günün $rainyCount günü yağış geçişi bekleniyor.")
                }

                val subText = "Yüksek $todayMax° / Düşük $todayMin°  •  Hissedilen $curFeel°"

                getSharedPreferences("SamsungOneUiWeather", Context.MODE_PRIVATE).edit()
                    .putString("city", resolvedName)
                    .putString("temp", "$curTemp°")
                    .putString("icon", curIcon)
                    .putString("cond", curDesc)
                    .putString("sub", subText)
                    .putString("alert", alertText)
                    .apply()

                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                runOnUiThread {
                    tvCity.text = resolvedName
                    tvBigTemp.text = "$curTemp°"
                    tvBigIcon.text = curIcon
                    tvCondition.text = curDesc
                    tvFeelsAndRange.text = subText
                    tvAlertBanner.text = alertText

                    tvMetricWind.text = "💨 Rüzgar\n$curWind km/s"
                    tvMetricHum.text = "💧 Nem\n%$curHum"
                    tvMetricUv.text = "☀️ UV İndeksi\n$todayUv"
                    tvMetricPress.text = "⏲️ Basınç\n$curPress hPa"

                    hourlyContainer.removeAllViews()
                    val startIdx = currentHour.coerceIn(0, 23)
                    for (h in startIdx until minOf(startIdx + 24, hTimes.length())) {
                        val rawTime = hTimes.getString(h).substringAfter("T")
                        val hTemp = rInt(hTemps.getDouble(h))
                        val hCode = hCodes.getInt(h)
                        val hRain = if (hProbs != null && !hProbs.isNull(h)) hProbs.getInt(h) else 0
                        hourlyContainer.addView(
                            createHourCard(if (h == startIdx) "Şimdi" else rawTime, hCode, hTemp, hRain, false)
                        )
                    }

                    dailyContainer.removeAllViews()
                    openDayContainer = null
                    openDayArrowView = null

                    for (i in 0 until minOf(7, dates.length())) {
                        val dayTitle = if (i == 0) "Bugün" else formatDayName(dates.getString(i))
                        val maxT = rInt(maxs.getDouble(i))
                        val minT = rInt(mins.getDouble(i))
                        val dCode = dCodes.getInt(i)
                        val rProb = if (rainProbs != null && !rainProbs.isNull(i)) rainProbs.getInt(i) else 0
                        val rSum = if (rainSums != null && !rainSums.isNull(i)) String.format(Locale.US, "%.1f", rainSums.getDouble(i)) else "0.0"
                        val wMax = if (maxWinds != null && !maxWinds.isNull(i)) rInt(maxWinds.getDouble(i)) else 0
                        val sRise = if (sunrises != null && !sunrises.isNull(i)) sunrises.getString(i).substringAfter("T") else "--:--"
                        val sSet = if (sunsets != null && !sunsets.isNull(i)) sunsets.getString(i).substringAfter("T") else "--:--"

                        val dayCard = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            background = cardStyle("#192B52", "#2B4680", 24f)
                            setPadding(dp(16), dp(14), dp(16), dp(14))
                            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
                        }

                        val summaryRow = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        val tvDay = label(dayTitle, 15.5f, "#FFFFFF", true).apply {
                            layoutParams = LinearLayout.LayoutParams(0, -2, 1.3f)
                        }
                        val tvRainBadge = label(if (rProb > 0) "💧 %$rProb" else "", 13f, "#60A5FA", true).apply {
                            layoutParams = LinearLayout.LayoutParams(0, -2, 0.65f)
                        }
                        val tvMidIcon = label(getIcon(dCode), 22f, "#FFFFFF", false).apply {
                            setPadding(0, 0, dp(12), 0)
                        }
                        val baseTempStr = "$maxT° / $minT°"
                        val tvMaxMin = label("$baseTempStr  ▾", 15f, "#E2E8F0", true)

                        summaryRow.addView(tvDay)
                        summaryRow.addView(tvRainBadge)
                        summaryRow.addView(tvMidIcon)
                        summaryRow.addView(tvMaxMin)

                        val dayDetailBox = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            visibility = View.GONE
                            setPadding(0, dp(12), 0, dp(4))
                        }

                        val toggleDay = {
                            if (dayDetailBox.visibility == View.VISIBLE) {
                                dayDetailBox.removeAllViews()
                                dayDetailBox.visibility = View.GONE
                                tvMaxMin.text = "$baseTempStr  ▾"
                                openDayContainer = null
                            } else {
                                openDayContainer?.removeAllViews()
                                openDayContainer?.visibility = View.GONE
                                openDayArrowView?.text = "$openDayBaseText  ▾"

                                buildDay24HourStrip(
                                    dayDetailBox, i, dayTitle, hTimes, hTemps, hProbs, hCodes,
                                    rSum, wMax, sRise, sSet
                                )
                                dayDetailBox.visibility = View.VISIBLE
                                tvMaxMin.text = "$baseTempStr  ▴"

                                openDayContainer = dayDetailBox
                                openDayArrowView = tvMaxMin
                                openDayBaseText = baseTempStr
                            }
                        }

                        dayCard.setOnClickListener { toggleDay() }

                        dayCard.addView(summaryRow)
                        dayCard.addView(dayDetailBox)
                        dailyContainer.addView(dayCard)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Şehir bulunamadı veya bağlantı hatası", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buildDay24HourStrip(
        container: LinearLayout,
        dayIndex: Int,
        dayTitle: String,
        hTimes: JSONArray,
        hTemps: JSONArray,
        hProbs: JSONArray?,
        hCodes: JSONArray,
        rSum: String,
        wMax: Int,
        sRise: String,
        sSet: String
    ) {
        container.removeAllViews()

        val divider = View(this).apply {
            setBackgroundColor(Color.parseColor("#2B4680"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply { bottomMargin = dp(10) }
        }
        container.addView(divider)

        container.addView(
            label("🕒 $dayTitle — Saat Başında Hava Durumu (00:00 - 23:00)", 12.5f, "#93C5FD", true).apply {
                setPadding(0, 0, 0, dp(8))
            }
        )

        val dayScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val startHour = dayIndex * 24
        val endHour = minOf(startHour + 24, hTimes.length())

        for (h in startHour until endHour) {
            val hourClock = hTimes.getString(h).substringAfter("T")
            val tempVal = rInt(hTemps.getDouble(h))
            val codeVal = hCodes.getInt(h)
            val probVal = if (hProbs != null && !hProbs.isNull(h)) hProbs.getInt(h) else 0

            row.addView(createHourCard(hourClock, codeVal, tempVal, probVal, true))
        }

        dayScroll.addView(row)
        container.addView(dayScroll)

        val footerText = "🌧️ Toplam Yağış: $rSum mm  •  💨 Rüzgar: $wMax km/s  •  🌅 $sRise / 🌇 $sSet"
        container.addView(label(footerText, 11.5f, "#CBD5E1", false))
    }

    private fun createHourCard(timeTxt: String, code: Int, temp: Int, rainProb: Int, isInsideDay: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            if (isInsideDay) {
                background = cardStyle("#101E3D", "#243B6B", 16f)
                setPadding(dp(10), dp(10), dp(10), dp(10))
                layoutParams = LinearLayout.LayoutParams(dp(84), -2).apply { rightMargin = dp(8) }
            } else {
                setPadding(dp(10), dp(4), dp(10), dp(4))
                layoutParams = LinearLayout.LayoutParams(dp(80), -2)
            }

            addView(label(timeTxt, 12f, "#E2E8F0", true))
            addView(label(getIcon(code), 24f, "#FFFFFF", false).apply { setPadding(0, dp(4), 0, dp(2)) })
            addView(label(getShortDesc(code), 11f, "#93C5FD", true).apply { gravity = Gravity.CENTER })
            addView(label("$temp°", 15.5f, "#FFFFFF", true).apply { setPadding(0, dp(3), 0, dp(2)) })
            addView(label(if (rainProb > 0) "💧%$rainProb" else " ", 11f, "#60A5FA", false))
        }
    }

    private fun rInt(d: Double): Int = Math.round(d).toInt()

    private fun httpGet(urlStr: String): String {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun getStatusBarHeight(): Int {
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) resources.getDimensionPixelSize(resId) else dp(24)
    }

    private fun cardStyle(fillHex: String, strokeHex: String, radiusDp: Float) = GradientDrawable().apply {
        setColor(Color.parseColor(fillHex))
        setStroke(dp(1), Color.parseColor(strokeHex))
        cornerRadius = dp(radiusDp.toInt()).toFloat()
    }

    private fun label(txt: String, sizeSp: Float, hex: String, bold: Boolean) = TextView(this).apply {
        text = txt
        textSize = sizeSp
        setTextColor(Color.parseColor(hex))
        if (bold) setTypeface(null, Typeface.BOLD)
    }

    private fun sectionHeader(txt: String) = label(txt, 14.5f, "#93C5FD", true).apply {
        setPadding(dp(6), 0, 0, dp(8))
    }

    private fun metricBox(title: String, value: String, isLeft: Boolean) = TextView(this).apply {
        text = "$title\n$value"
        textSize = 15f
        setTextColor(Color.WHITE)
        background = cardStyle("#192B52", "#2B4680", 22f)
        setPadding(dp(16), dp(16), dp(16), dp(16))
        setLineSpacing(dp(6).toFloat(), 1f)
        layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply {
            if (isLeft) rightMargin = dp(5) else leftMargin = dp(5)
        }
    }

    private fun formatDayName(iso: String): String = try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)
        SimpleDateFormat("EEEE, d MMM", Locale("tr", "TR")).format(date!!)
    } catch (e: Exception) {
        iso
    }

    private fun getIcon(c: Int): String = when (c) {
        0 -> "☀️"
        1, 2 -> "🌤️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75, 77 -> "❄️"
        80, 81, 82 -> "🌧️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }

    private fun getShortDesc(c: Int): String = when (c) {
        0 -> "Açık"
        1, 2 -> "Az Bulutlu"
        3 -> "Bulutlu"
        45, 48 -> "Sisli"
        51, 53, 55 -> "Çisenti"
        61, 63, 65 -> "Yağışlı"
        71, 73, 75, 77 -> "Karlı"
        80, 81, 82 -> "Sağanak"
        95, 96, 99 -> "Fırtına"
        else -> "Normal"
    }

    private fun getDesc(c: Int): String = when (c) {
        0 -> "Açık ve Güneşli"
        1, 2 -> "Parçalı Bulutlu"
        3 -> "Çok Bulutlu"
        45, 48 -> "Sisli"
        51, 53, 55 -> "Çisenti"
        61, 63, 65 -> "Yağmurlu"
        71, 73, 75, 77 -> "Kar Yağışlı"
        80, 81, 82 -> "Sağanak Yağışlı"
        95, 96, 99 -> "Fırtına / Gök Gürültülü"
        else -> "Normal"
    }
}