
package com.senseai.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
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

    private val languages =
        arrayOf("O‘zbekcha", "Русский", "English")

    private lateinit var languageSpinner: Spinner
    private lateinit var questionInput: EditText
    private lateinit var sendButton: Button
    private lateinit var answerText: TextView
    private lateinit var statusText: TextView

    private var selectedLanguage = "uz"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 35, 24, 30)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "SENSE AI"
            textSize = 30f
            setTextColor(Color.rgb(25, 75, 160))
            gravity = Gravity.CENTER
        }

        languageSpinner = Spinner(this)

        languageSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languages
        )

        val subtitle = TextView(this).apply {
            textSize = 21f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 10)
        }

        val description = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }

        questionInput = EditText(this).apply {
            textSize = 16f
            minLines = 2
            maxLines = 5
            gravity = Gravity.TOP
            hint = "Savolingizni yozing..."
            setPadding(15, 15, 15, 15)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.rgb(245, 247, 250))
                cornerRadius = 18f
                setStroke(1, Color.LTGRAY)
            }
        }

        sendButton = Button(this).apply {
            text = "AI ga yuborish"
        }

        statusText = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 10)
        }

        answerText = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.rgb(35, 35, 35))
            setPadding(16, 16, 16, 16)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.rgb(242, 246, 252))
                cornerRadius = 18f
            }
            visibility = View.GONE
        }

        val camera = Button(this)
        val voice = Button(this)
        val lessons = Button(this)
        val progress = Button(this)

        fun updateLanguage(position: Int) {
            when (position) {
                0 -> {
                    selectedLanguage = "uz"
                    subtitle.text = "Bugun nimani o‘rganamiz?"
                    description.text =
                        "Savolingizni yozing va SENSE AI dan javob oling."
                    questionInput.hint = "Savolingizni yozing..."
                    sendButton.text = "AI ga yuborish"
                    camera.text = "📷 Kamera orqali o‘rganish"
                    voice.text = "🎙 Ovoz orqali savol berish"
                    lessons.text = "📚 Mening darslarim"
                    progress.text = "📊 Bilimlarim va natijalarim"
                }

                1 -> {
                    selectedLanguage = "ru"
                    subtitle.text = "Что будем изучать сегодня?"
                    description.text =
                        "Напишите вопрос и получите ответ от SENSE AI."
                    questionInput.hint = "Напишите свой вопрос..."
                    sendButton.text = "Отправить AI"
                    camera.text = "📷 Обучение через камеру"
                    voice.text = "🎙 Задать вопрос голосом"
                    lessons.text = "📚 Мои уроки"
                    progress.text = "📊 Мои знания и результаты"
                }

                2 -> {
                    selectedLanguage = "en"
                    subtitle.text = "What shall we learn today?"
                    description.text =
                        "Write your question and get an answer from SENSE AI."
                    questionInput.hint = "Write your question..."
                    sendButton.text = "Ask AI"
                    camera.text = "📷 Learn with camera"
                    voice.text = "🎙 Ask by voice"
                    lessons.text = "📚 My lessons"
                    progress.text = "📊 My progress"
                }
            }
        }

        sendButton.setOnClickListener {
            sendQuestion()
        }

        camera.setOnClickListener {
            Toast.makeText(
                this,
                "Kamera funksiyasini keyingi bosqichda ulaymiz",
                Toast.LENGTH_SHORT
            ).show()
        }

        voice.setOnClickListener {
            Toast.makeText(
                this,
                "Ovoz funksiyasini keyingi bosqichda ulaymiz",
                Toast.LENGTH_SHORT
            ).show()
        }

        lessons.setOnClickListener {
            Toast.makeText(
                this,
                "Darslar bo‘limi keyingi bosqichda",
                Toast.LENGTH_SHORT
            ).show()
        }

        progress.setOnClickListener {
            Toast.makeText(
                this,
                "Natijalar bo‘limi keyingi bosqichda",
                Toast.LENGTH_SHORT
            ).show()
        }

        root.addView(title)
        root.addView(languageSpinner)
        root.addView(subtitle)
        root.addView(description)

        root.addView(questionInput, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 25
        })

        root.addView(sendButton)
        root.addView(statusText)
        root.addView(answerText)

        root.addView(camera)
        root.addView(voice)
        root.addView(lessons)
        root.addView(progress)

        languageSpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    updateLanguage(position)
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}
            }

        scroll.addView(root)
        setContentView(scroll)

        updateLanguage(0)
    }

    private fun sendQuestion() {
        val question = questionInput.text.toString().trim()

        if (question.isEmpty()) {
            questionInput.error = "Savol yozing"
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
                    put("language", selectedLanguage)
                }

                connection.outputStream.use { output ->
                    output.write(
                        request.toString().toByteArray(Charsets.UTF_8)
                    )
                }

                val responseCode = connection.responseCode

                val responseStream =
                    if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val responseBody = responseStream
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    ?: ""

                val result = JSONObject(responseBody)

                if (responseCode in 200..299 &&
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
                        "Server xatosi: $responseCode"
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

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
