
package com.senseai.app

import android.app.Activity
import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val serverUrl =
        "https://sense-ai-server.ayubxonsafaxonov3.workers.dev/ask"

    private val executor = Executors.newSingleThreadExecutor()
    private val prefs by lazy {
        getSharedPreferences("sense_ai_data", MODE_PRIVATE)
    }

    private val blue = Color.rgb(37, 99, 235)
    private val dark = Color.rgb(15, 29, 56)
    private val gray = Color.rgb(107, 114, 128)

    private var language = "uz"
    private var lastQuestion = ""
    private var lastAnswer = ""
    private var ttsReady = false

    private lateinit var questionInput: EditText
    private lateinit var answerText: TextView
    private lateinit var statusText: TextView
    private lateinit var sendButton: Button
    private lateinit var voiceButton: Button
    private lateinit var cameraButton: Button
    private lateinit var speakButton: Button
    private lateinit var saveButton: Button
    private lateinit var lessonsButton: Button
    private lateinit var statsButton: Button
    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var servicesTitle: TextView

    private lateinit var tts: TextToSpeech
    private var speechRecognizer: SpeechRecognizer? = null

    companion object {
        private const val CAMERA_REQUEST = 101
        private const val AUDIO_PERMISSION_REQUEST = 102
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun rounded(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun text(
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

    private fun button(
        value: String,
        color: Int = blue
    ): Button {
        return Button(this).apply {
            text = value
            textSize = 13f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(color, 14)
            minHeight = dp(48)
            setPadding(dp(8), dp(5), dp(8), dp(5))
        }
    }

    private fun tr(uz: String, ru: String, en: String): String {
        return when (language) {
            "ru" -> ru
            "en" -> en
            else -> uz
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { result ->
            ttsReady = result == TextToSpeech.SUCCESS
            if (ttsReady) updateTtsLanguage()
        }

        buildScreen()
        setLanguage("uz")
    }

    private fun buildScreen() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(244, 247, 252))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(30))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(21), dp(22), dp(21), dp(23))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(15, 29, 56),
                    Color.rgb(30, 64, 125)
                )
            ).apply {
                cornerRadius = dp(24).toFloat()
            }
        }

        val brandRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        titleText = text("SENSE AI", 26f, Color.WHITE, true)
        brandRow.addView(
            titleText,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        brandRow.addView(
            text("● AI ONLINE", 12f, Color.rgb(134, 239, 172), true)
        )

        subtitleText = text(
            "Bugun nimani kashf qilamiz?",
            22f,
            Color.WHITE,
            true
        ).apply {
            setPadding(0, dp(26), 0, dp(8))
        }

        val headerDescription = text(
            "Savolingizni yozing va yangi bilim oling.",
            14f,
            Color.rgb(210, 222, 243)
        )

        header.addView(brandRow)
        header.addView(subtitleText)
        header.addView(headerDescription)
        root.addView(header)

        val languageRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(16), 0, dp(16))
        }

        val languages = listOf(
            "O‘Z" to "uz",
            "РУ" to "ru",
            "EN" to "en"
        )

        languages.forEach { item ->
            val b = button(item.first, Color.WHITE).apply {
                setTextColor(dark)
                setOnClickListener {
                    setLanguage(item.second)
                }
            }
            val p = LinearLayout.LayoutParams(0, dp(46), 1f)
            p.marginEnd = dp(6)
            languageRow.addView(b, p)
        }

        root.addView(languageRow)

        val questionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(Color.WHITE, 20)
            elevation = dp(3).toFloat()
        }

        questionCard.addView(
            text("✦  SENSE AI DAN SO‘RANG", 14f, blue, true)
        )

        questionInput = EditText(this).apply {
            hint = "Savolingizni shu yerga yozing..."
            textSize = 16f
            minLines = 3
            maxLines = 5
            gravity = Gravity.TOP
            setTextColor(dark)
            setHintTextColor(gray)
            setPadding(dp(13), dp(13), dp(13), dp(13))
            background = rounded(Color.rgb(246, 248, 252), 13)
        }

        val inputParams = LinearLayout.LayoutParams(-1, -2)
        inputParams.topMargin = dp(14)
        questionCard.addView(questionInput, inputParams)

        sendButton = button("AI GA YUBORISH  →")
        sendButton.setOnClickListener {
            askAI(questionInput.text.toString().trim(), null)
        }

        val sendParams = LinearLayout.LayoutParams(-1, dp(52))
        sendParams.topMargin = dp(12)
        questionCard.addView(sendButton, sendParams)

        statusText = text(
            "Savolingizga javob olishga tayyormiz.",
            12f,
            gray
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(8))
        }

        answerText = text("", 16f, dark).apply {
            setPadding(dp(15), dp(15), dp(15), dp(15))
            background = rounded(Color.rgb(235, 243, 255), 16)
            visibility = View.GONE
        }

        root.addView(questionCard)
        root.addView(statusText)
        root.addView(answerText)

        servicesTitle = text(
            "O‘RGANISH BO‘LIMLARI",
            16f,
            dark,
            true
        ).apply {
            setPadding(0, dp(24), 0, dp(12))
        }
        root.addView(servicesTitle)

        cameraButton = button("📷  KAMERA AI")
        cameraButton.setOnClickListener { openCamera() }
        addFullButton(root, cameraButton)

        voiceButton = button("🎙  OVOZ ORQALI SAVOL", Color.rgb(22, 132, 90))
        voiceButton.setOnClickListener { startVoiceInput() }
        addFullButton(root, voiceButton)

        speakButton = button("🔊  JAVOBNI ESHITISH", Color.rgb(118, 82, 181))
        speakButton.setOnClickListener { speakAnswer() }
        addFullButton(root, speakButton)

        saveButton = button("💾  JAVOBNI SAQLASH", Color.rgb(180, 110, 25))
        saveButton.setOnClickListener { saveLesson() }
        addFullButton(root, saveButton)

        lessonsButton = button("📚  MENING DARSLARIM", dark)
        lessonsButton.setOnClickListener { showLessons() }
        addFullButton(root, lessonsButton)

        statsButton = button("📊  BILIMLARIM VA NATIJALARIM", dark)
        statsButton.setOnClickListener { showStats() }
        addFullButton(root, statsButton)

        root.addView(
            text(
                "SENSE AI  •  BILIM SARI BIR QADAM",
                11f,
                gray
            ).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(22), 0, dp(5))
            }
        )

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun addFullButton(root: LinearLayout, b: Button) {
        val p = LinearLayout.LayoutParams(-1, dp(52))
        p.topMargin = dp(9)
        root.addView(b, p)
    }

    private fun setLanguage(code: String) {
        language = code

        subtitleText.text = tr(
            "Bugun nimani kashf qilamiz?",
            "Что будем изучать сегодня?",
            "What shall we discover today?"
        )

        questionInput.hint = tr(
            "Savolingizni shu yerga yozing...",
            "Напишите свой вопрос...",
            "Write your question here..."
        )

        sendButton.text = tr(
            "AI GA YUBORISH  →",
            "ОТПРАВИТЬ ВОПРОС  →",
            "ASK SENSE AI  →"
        )

        servicesTitle.text = tr(
            "O‘RGANISH BO‘LIMLARI",
            "РАЗДЕЛЫ ОБУЧЕНИЯ",
            "LEARNING TOOLS"
        )

        cameraButton.text = tr(
            "📷  KAMERA AI",
            "📷  КАМЕРА AI",
            "📷  CAMERA AI"
        )

        voiceButton.text = tr(
            "🎙  OVOZ ORQALI SAVOL",
            "🎙  ГОЛОСОВОЙ ВОПРОС",
            "🎙  ASK BY VOICE"
        )

        speakButton.text = tr(
            "🔊  JAVOBNI ESHITISH",
            "🔊  ПРОСЛУШАТЬ ОТВЕТ",
            "🔊  LISTEN TO ANSWER"
        )

        saveButton.text = tr(
            "💾  JAVOBNI SAQLASH",
            "💾  СОХРАНИТЬ ОТВЕТ",
            "💾  SAVE ANSWER"
        )

        lessonsButton.text = tr(
            "📚  MENING DARSLARIM",
            "📚  МОИ УРОКИ",
            "📚  MY LESSONS"
        )

        statsButton.text = tr(
            "📊  BILIMLARIM VA NATIJALARIM",
            "📊  МОИ ЗНАНИЯ И РЕЗУЛЬТАТЫ",
            "📊  MY PROGRESS"
        )

        updateTtsLanguage()
    }
    
    // AI SAVOL-JAVOB
    private fun askAI(questionValue: String, imageBase64: String?) {
        var question = questionValue

        if (question.isBlank() && imageBase64 == null) {
            questionInput.error = tr(
                "Avval savol yozing",
                "Сначала напишите вопрос",
                "Please write a question first"
            )
            return
        }

        if (imageBase64 != null && question.isBlank()) {
            question = tr(
                "Rasmda nima borligini tushuntirib bering.",
                "Объясните, что изображено на картинке.",
                "Please explain what is in this picture."
            )
        }

        if (question.length > 2000) {
            questionInput.error = "Savol 2000 belgidan oshmasin"
            return
        }

        lastQuestion = question
        sendButton.isEnabled = false
        statusText.text = tr(
            "SENSE AI javob tayyorlamoqda...",
            "SENSE AI готовит ответ...",
            "SENSE AI is preparing an answer..."
        )
        answerText.visibility = View.GONE

        val selectedLanguage = language

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

                    if (imageBase64 != null) {
                        put("image_base64", imageBase64)
                        put("image_type", "image/jpeg")
                    }
                }

                connection.outputStream.use {
                    it.write(request.toString().toByteArray(Charsets.UTF_8))
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
                        lastAnswer = answer
                        answerText.text = answer
                        answerText.visibility = View.VISIBLE
                        statusText.text = tr(
                            "Javob tayyor",
                            "Ответ готов",
                            "Answer ready"
                        )
                        sendButton.isEnabled = true

                        val count = prefs.getInt("question_count", 0) + 1
                        prefs.edit().putInt("question_count", count).apply()
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
                    statusText.text = tr(
                        "Ulanishda xatolik. Internetni tekshiring.",
                        "Ошибка соединения. Проверьте интернет.",
                        "Connection error. Check your internet."
                    )
                    sendButton.isEnabled = true
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    // KAMERA
    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQUEST)
        } catch (e: Exception) {
            toast(tr(
                "Kamera ilovasi topilmadi",
                "Приложение камеры не найдено",
                "Camera app not found"
            ))
        }
    }

    @Deprecated("Using activity result for broad Android compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as? Bitmap

            if (bitmap == null) {
                toast(tr(
                    "Rasm olinmadi. Qayta urinib ko‘ring.",
                    "Фото не получено. Попробуйте ещё раз.",
                    "No image received. Please try again."
                ))
                return
            }

            statusText.text = tr(
                "Rasm AI ga yuborilmoqda...",
                "Изображение отправляется в AI...",
                "Sending image to AI..."
            )

            val imageData = bitmapToBase64(bitmap)
            askAI(questionInput.text.toString().trim(), imageData)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val maxSize = 768
        val scale = minOf(
            1.0,
            maxSize.toDouble() / maxOf(bitmap.width, bitmap.height)
        )

        val resized = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )

        val output = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 65, output)

        return Base64.encodeToString(
            output.toByteArray(),
            Base64.NO_WRAP
        )
    }

    // OVOZNI MATNGA AYLANTIRISH
    private fun startVoiceInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            toast(tr(
                "Telefoningizda ovozni aniqlash xizmati topilmadi.",
                "На телефоне не найдена служба распознавания речи.",
                "Speech recognition is unavailable on this phone."
            ))
            return
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                AUDIO_PERMISSION_REQUEST
            )
            return
        }

        launchSpeechRecognizer()
    }

    private fun launchSpeechRecognizer() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        statusText.text = tr(
                            "Gapiring...",
                            "Говорите...",
                            "Speak now..."
                        )
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        statusText.text = tr(
                            "Ovoz qayta ishlanmoqda...",
                            "Обработка голоса...",
                            "Processing voice..."
                        )
                    }

                    override fun onError(error: Int) {
                        statusText.text = tr(
                            "Ovoz aniqlanmadi. Qayta urinib ko‘ring.",
                            "Голос не распознан. Попробуйте ещё раз.",
                            "Speech not recognized. Try again."
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                        val spoken = matches?.firstOrNull()

                        if (!spoken.isNullOrBlank()) {
                            questionInput.setText(spoken)
                            askAI(spoken, null)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                }
            )

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    when (language) {
                        "ru" -> "ru-RU"
                        "en" -> "en-US"
                        else -> "uz-UZ"
                    }
                )
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)

        } catch (e: Exception) {
            toast(tr(
                "Ovoz xizmatini ishga tushirib bo‘lmadi.",
                "Не удалось запустить голосовую службу.",
                "Could not start speech recognition."
            ))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == AUDIO_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchSpeechRecognizer()
            } else {
                toast(tr(
                    "Ovoz funksiyasi uchun mikrofon ruxsati kerak.",
                    "Для голосовой функции нужен доступ к микрофону.",
                    "Microphone permission is needed for voice input."
                ))
            }
        }
    }

    // JAVOBNI OVOZDA O‘QISH
    private fun updateTtsLanguage() {
        if (!ttsReady) return

        val locale = when (language) {
            "ru" -> Locale("ru", "RU")
            "en" -> Locale.US
            else -> Locale("uz", "UZ")
        }

        tts.language = locale
    }

    private fun speakAnswer() {
        if (lastAnswer.isBlank()) {
            toast(tr(
                "Avval AI dan javob oling.",
                "Сначала получите ответ от AI.",
                "Get an AI answer first."
            ))
            return
        }

        if (!ttsReady) {
            toast(tr(
                "Ovozli o‘qish xizmati tayyor emas.",
                "Служба озвучивания не готова.",
                "Text-to-speech is not ready."
            ))
            return
        }

        val result = tts.speak(
            lastAnswer,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "sense_ai_answer"
        )

        if (result == TextToSpeech.ERROR) {
            toast(tr(
                "Bu til uchun ovoz mavjud bo‘lmasligi mumkin.",
                "Для этого языка может не быть голоса.",
                "A voice may not be available for this language."
            ))
        }
    }

    // DARSLARNI SAQLASH
    private fun saveLesson() {
        if (lastAnswer.isBlank()) {
            toast(tr(
                "Saqlash uchun avval AI javobi kerak.",
                "Сначала получите ответ AI.",
                "Get an AI answer before saving."
            ))
            return
        }

        val lessons = loadLessons()
        val lesson = JSONObject().apply {
            put("question", lastQuestion)
            put("answer", lastAnswer)
            put(
                "date",
                SimpleDateFormat(
                    "dd.MM.yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date())
            )
        }

        lessons.put(lesson)
        prefs.edit().putString("lessons", lessons.toString()).apply()

        toast(tr(
            "Darslaringizga saqlandi!",
            "Сохранено в мои уроки!",
            "Saved to My Lessons!"
        ))
    }

    private fun loadLessons(): JSONArray {
        return try {
            JSONArray(prefs.getString("lessons", "[]"))
        } catch (e: Exception) {
            JSONArray()
        }
    }

    private fun showLessons() {
        val lessons = loadLessons()

        if (lessons.length() == 0) {
            toast(tr(
                "Hozircha saqlangan darslar yo‘q.",
                "Пока нет сохранённых уроков.",
                "There are no saved lessons yet."
            ))
            return
        }

        val titles = ArrayList<String>()

        for (i in 0 until lessons.length()) {
            val item = lessons.getJSONObject(i)
            titles.add(item.optString("question", "Dars ${i + 1}"))
        }

        AlertDialog.Builder(this)
            .setTitle(tr(
                "Mening darslarim",
                "Мои уроки",
                "My Lessons"
            ))
            .setItems(titles.toTypedArray()) { _, index ->
                showLessonDetails(index)
            }
            .setNegativeButton(tr("Yopish", "Закрыть", "Close"), null)
            .show()
    }

    private fun showLessonDetails(index: Int) {
        val lessons = loadLessons()
        if (index !in 0 until lessons.length()) return

        val item = lessons.getJSONObject(index)
        val question = item.optString("question")
        val answer = item.optString("answer")
        val date = item.optString("date")

        AlertDialog.Builder(this)
            .setTitle(question)
            .setMessage("$answer\n\n$date")
            .setPositiveButton(
                tr("O‘qish", "Читать", "Read")
            ) { _, _ ->
                lastQuestion = question
                lastAnswer = answer
                answerText.text = answer
                answerText.visibility = View.VISIBLE
                questionInput.setText(question)
            }
            .setNegativeButton(
                tr("O‘chirish", "Удалить", "Delete")
            ) { _, _ ->
                val updated = JSONArray()
                for (i in 0 until lessons.length()) {
                    if (i != index) updated.put(lessons.get(i))
                }
                prefs.edit().putString("lessons", updated.toString()).apply()
                toast(tr(
                    "Dars o‘chirildi",
                    "Урок удалён",
                    "Lesson deleted"
                ))
            }
            .setNeutralButton(tr("Yopish", "Закрыть", "Close"), null)
            .show()
    }

    // NATIJALAR
    private fun showStats() {
        val count = prefs.getInt("question_count", 0)
        val lessons = loadLessons()

        val message = tr(
            "Jami berilgan savollar: $count\n" +
                "Saqlangan darslar: ${lessons.length()}\n\n" +
                "Har kuni yangi bilim o‘rganishda davom eting!",
            "Всего задано вопросов: $count\n" +
                "Сохранённых уроков: ${lessons.length()}\n\n" +
                "Продолжайте узнавать новое каждый день!",
            "Questions asked: $count\n" +
                "Saved lessons: ${lessons.length()}\n\n" +
                "Keep learning something new every day!"
        )

        AlertDialog.Builder(this)
            .setTitle(tr(
                "Bilimlarim va natijalarim",
                "Мои знания и результаты",
                "My Progress"
            ))
            .setMessage(message)
            .setPositiveButton(tr("Yopish", "Закрыть", "Close"), null)
            .show()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        if (::tts.isInitialized) tts.shutdown()
        executor.shutdownNow()
        super.onDestroy()
    }
}
