package com.senseai.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val serverUrl =
        "https://sense-ai-server.ayubxonsafaxonov3.workers.dev/ask"

    private val executor = Executors.newSingleThreadExecutor()

    private val blue = Color.rgb(37, 99, 235)
    private val dark = Color.rgb(15, 29, 56)
    private val textDark = Color.rgb(31, 41, 55)
    private val gray = Color.rgb(107, 114, 128)

    private var selectedLanguage = "uz"

    private lateinit var questionInput: EditText
    private lateinit var answerText: TextView
    private lateinit var statusText: TextView
    private lateinit var sendButton: Button
    private lateinit var subtitle: TextView
    private lateinit var description: TextView
    private lateinit var cameraTitle: TextView
    private lateinit var cameraDescription: TextView
    private lateinit var voiceTitle: TextView
    private lateinit var voiceDescription: TextView
    private lateinit var lessonsTitle: TextView
    private lateinit var lessonsDescription: TextView
    private lateinit var progressTitle: TextView
    private lateinit var progressDescription: TextView

    private val languageButtons = mutableListOf<Button>()

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun rounded(
        color: Int,
        radius: Int,
        strokeColor: Int = Color.TRANSPARENT
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (strokeColor != Color.TRANSPARENT) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun makeText(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(244, 247, 252))
            clipToPadding = false
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(30))
        }

        // HEADER
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(25))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(15, 29, 56),
                    Color.rgb(30, 64, 125)
                )
            ).apply {
                cornerRadius = dp(25).toFloat()
            }
        }

        val brandRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val logo = makeText("SENSE AI", 25f, Color.WHITE, true)
        brandRow.addView(
            logo,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val online = TextView(this).apply {
            text = "● AI ONLINE"
            textSize = 11f
            setTextColor(Color.rgb(134, 239, 172))
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = rounded(
                Color.rgb(34, 65, 91), 30
            )
        }
        brandRow.addView(online)

        subtitle = makeText(
            "Bugun nimani kashf qilamiz?",
            23f,
            Color.WHITE,
            true
        ).apply {
            setPadding(0, dp(28), 0, dp(8))
        }

        description = makeText(
            "Savolingizni yozing va yangi bilim oling.",
            14f,
            Color.rgb(210, 222, 243)
        )

        header.addView(brandRow)
        header.addView(subtitle)
        header.addView(description)

        // LANGUAGE SELECTOR
        val languageRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, dp(18))
        }

        val languageNames = listOf(
            "O‘Z", "РУ", "EN"
        )

        val languageCodes = listOf(
            "uz", "ru", "en"
        )

        for (i in languageNames.indices) {
            val button = Button(this).apply {
                text = languageNames[i]
                textSize = 13f
                isAllCaps = false
                setPadding(dp(8), 0, dp(8), 0)
                minHeight = dp(42)
                background = rounded(Color.WHITE, 12)
                setTextColor(dark)
                setOnClickListener {
                    setLanguage(languageCodes[i])
                }
            }

            val params = LinearLayout.LayoutParams(
                0, dp(42), 1f
            )
            params.marginEnd = dp(7)
            languageRow.addView(button, params)
            languageButtons.add(button)
        }

        // QUESTION CARD
        val questionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(17), dp(17), dp(15))
            background = rounded(Color.WHITE, 20)
            elevation = dp(3).toFloat()
        }

        questionCard.addView(
            makeText("✦  SENSE AI DAN SO‘RANG", 13f, blue, true)
        )

        questionInput = EditText(this).apply {
            textSize = 16f
            minLines = 3
            maxLines = 5
            gravity = Gravity.TOP
            hint = "Savolingizni shu yerga yozing..."
            setTextColor(textDark)
            setHintTextColor(Color.rgb(156, 163, 175))
            setPadding(dp(13), dp(13), dp(13), dp(13))
            background = rounded(
                Color.rgb(246, 248, 252),
                13,
                Color.rgb(229, 234, 243)
            )
        }

        val questionParams = LinearLayout.LayoutParams(
            -1, -2
        )
        questionParams.topMargin = dp(14)
        questionCard.addView(questionInput, questionParams)

        sendButton = Button(this).apply {
            text = "AI GA YUBORISH  →"
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(blue, 14)
            setPadding(dp(12), dp(5), dp(12), dp(5))
            setOnClickListener {
                sendQuestion()
            }
        }

        val sendParams = LinearLayout.LayoutParams(
            -1, dp(52)
        )
        sendParams.topMargin = dp(12)
        questionCard.addView(sendButton, sendParams)

        statusText = makeText(
            "Savolingizga javob olishga tayyormiz.",
            12f,
            gray
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(4))
        }

        answerText = makeText("", 16f, textDark).apply {
            setPadding(dp(15), dp(15), dp(15), dp(15))
            background = rounded(
                Color.rgb(235, 243, 255), 15
            )
            visibility = View.GONE
        }

        // SERVICES TITLE
        val servicesTitle = makeText(
            "O‘RGANISH BO‘LIMLARI",
            16f,
            dark,
            true
        ).apply {
            setPadding(0, dp(25), 0, dp(13))
        }

        // SERVICE CARDS
        cameraTitle = makeText("📷 Kamera AI", 16f, dark, true)
        cameraDescription = makeText(
            "Rasm orqali o‘rganish", 12f, gray
        )

        voiceTitle = makeText("🎙 Ovozli AI", 16f, dark, true)
        voiceDescription = makeText(
            "Ovoz bilan savol berish", 12f, gray
        )

        lessonsTitle = makeText("📚 Darslarim", 16f, dark, true)
        lessonsDescription = makeText(
            "Saqlangan darslar", 12f, gray
        )

        progressTitle = makeText("📊 Natijalarim", 16f, dark, true)
        progressDescription = makeText(
            "Bilim va rivojlanish", 12f, gray
        )

        val card1 = serviceCard(
            "📷", cameraTitle, cameraDescription,
            Color.rgb(232, 241, 255)
        ) {
            toastMessage("Kamera funksiyasini keyingi bosqichda ulaymiz")
        }

        val card2 = serviceCard(
            "🎙", voiceTitle, voiceDescription,
            Color.rgb(231, 248, 240)
        ) {
            toastMessage("Ovoz funksiyasini keyingi bosqichda ulaymiz")
        }

        val card3 = serviceCard(
            "📚", lessonsTitle, lessonsDescription,
            Color.rgb(243, 237, 255)
        ) {
            toastMessage("Darslar bo‘limini keyingi bosqichda yaratamiz")
        }

        val card4 = serviceCard(
            "📊", progressTitle, progressDescription,
            Color.rgb(255, 243, 229)
        ) {
            toastMessage("Natijalar bo‘limini keyingi bosqichda yaratamiz")
        }

        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        row1.addView(card1, LinearLayout.LayoutParams(0, dp(125), 1f))
        val gap1 = View(this)
        row1.addView(gap1, LinearLayout.LayoutParams(dp(10), 1))
        row1.addView(card2, LinearLayout.LayoutParams(0, dp(125), 1f))

        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        row2.addView(card3, LinearLayout.LayoutParams(0, dp(125), 1f))
        val gap2 = View(this)
        row2.addView(gap2, LinearLayout.LayoutParams(dp(10), 1))
        row2.addView(card4, LinearLayout.LayoutParams(0, dp(125), 1f))

        val row2Params = LinearLayout.LayoutParams(-1, dp(125))
        row2Params.topMargin = dp(10)

        // FOOTER
        val footer = makeText(
            "SENSE AI  •  BILIM SARI BIR QADAM",
            11f,
            gray
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(25), 0, dp(5))
        }

        root.addView(header)
        root.addView(languageRow)
        root.addView(questionCard)

        root.addView(statusText)
        root.addView(answerText)

        root.addView(servicesTitle)
        root.addView(row1)
        root.addView(row2, row2Params)
        root.addView(footer)

        scroll.addView(root)
        setContentView(scroll)

        setLanguage("uz")
    }

    private fun serviceCard(
        icon: String,
        title: TextView,
        description: TextView,
        color: Int,
        action: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(13), dp(12), dp(10), dp(12))
            background = rounded(Color.WHITE, 18)
            elevation = dp(2).toFloat()

            addView(
                makeText(icon, 23f, dark, true)
            )

            val titleParams = LinearLayout.LayoutParams(-1, -2)
            titleParams.topMargin = dp(8)
            addView(title, titleParams)

            val descParams = LinearLayout.LayoutParams(-1, -2)
            descParams.topMargin = dp(4)
            addView(description, descParams)

            setOnClickListener { action() }
        }
    }

    private fun setLanguage(language: String) {
        selectedLanguage = language

        val selectedIndex = when (language) {
            "ru" -> 1
            "en" -> 2
            else -> 0
        }

        for (i in languageButtons.indices) {
            val selected = i == selectedIndex
            languageButtons[i].background = rounded(
                if (selected) blue else Color.WHITE, 12
            )
            languageButtons[i].setTextColor(
                if (selected) Color.WHITE else dark
            )
        }

        when (language) {
            "ru" -> {
                subtitle.text = "Что будем изучать сегодня?"
                description.text = "Задайте вопрос и получайте новые знания."
                questionInput.hint = "Напишите свой вопрос..."
                sendButton.text = "ОТПРАВИТЬ ВОПРОС  →"
                cameraTitle.text = "📷 Камера AI"
                cameraDescription.text = "Обучение по изображению"
                voiceTitle.text = "🎙 Голосовой AI"
                voiceDescription.text = "Задайте вопрос голосом"
                lessonsTitle.text = "📚 Мои уроки"
                lessonsDescription.text = "Сохранённые уроки"
                progressTitle.text = "📊 Мои результаты"
                progressDescription.text = "Знания и развитие"
            }

            "en" -> {
                subtitle.text = "What shall we discover today?"
                description.text = "Ask a question and learn something new."
                questionInput.hint = "Write your question here..."
                sendButton.text = "ASK SENSE AI  →"
                cameraTitle.text = "📷 Camera AI"
                cameraDescription.text = "Learn from images"
                voiceTitle.text = "🎙 Voice AI"
                voiceDescription.text = "Ask using your voice"
                lessonsTitle.text = "📚 My lessons"
                lessonsDescription.text = "Saved lessons"
                progressTitle.text = "📊 My progress"
                progressDescription.text = "Knowledge and progress"
            }

            else -> {
                subtitle.text = "Bugun nimani kashf qilamiz?"
                description.text = "Savolingizni yozing va yangi bilim oling."
                questionInput.hint = "Savolingizni shu yerga yozing..."
                sendButton.text = "AI GA YUBORISH  →"
                cameraTitle.text = "📷 Kamera AI"
                cameraDescription.text = "Rasm orqali o‘rganish"
                voiceTitle.text = "🎙 Ovozli AI"
                voiceDescription.text = "Ovoz bilan savol berish"
                lessonsTitle.text = "📚 Darslarim"
                lessonsDescription.text = "Saqlangan darslar"
                progressTitle.text = "📊 Natijalarim"
                progressDescription.text = "Bilim va rivojlanish"
            }
        }
    }

    private fun sendQuestion() {
        val question = questionInput.text.toString().trim()
        val language = selectedLanguage

        if (question.isEmpty()) {
            questionInput.error = "Avval savol yozing"
            return
        }

        if (question.length > 2000) {
            questionInput.error = "Savol 2000 belgidan oshmasin"
            return
        }

        sendButton.isEnabled = false
        statusText.text = "SENSE AI javob tayyorlamoqda..."
        answerText.visibility = View.GONE

        executor.execute {
            var connection: HttpURLConnection? = null

            try {
                connection = URL(serverUrl)
                    .openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 20000
                connection.readTimeout = 60000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                )

                val request = JSONObject().apply {
                    put("question", question)
                    put("language", language)
                }

                connection.outputStream.use { output ->
                    output.write(
                        request.toString().toByteArray(Charsets.UTF_8)
                    )
                }

                val code = connection.responseCode

                val stream = if (code in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val body = stream?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() } ?: ""

                val result = JSONObject(body)

                if (code in 200..299 &&
                    result.optBoolean("success")) {

                    val answer = result.optString("answer")

                    runOnUiThread {
                        answerText.text = answer
                        answerText.visibility = View.VISIBLE
                        statusText.text = "Javob tayyor"
                        sendButton.isEnabled = true
                    }
                } else {
                    val error = result.optString(
                        "error",
                        "Server xatosi: $code"
                    )

                    runOnUiThread {
                        statusText.text = error
                        sendButton.isEnabled = true
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text =
                        "Ulanishda xatolik. Internetni tekshiring."
                    sendButton.isEnabled = true
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun toastMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}

