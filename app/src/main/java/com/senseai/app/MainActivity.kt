
package com.senseai.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
 
class MainActivity : Activity() {

    private val languages = arrayOf("O‘zbekcha", "Русский", "English")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 40, 24, 24)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "SENSE AI"
            textSize = 30f
            setTextColor(Color.rgb(25, 75, 160))
            gravity = Gravity.CENTER
        }

        val languageSpinner = Spinner(this)
        languageSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languages
        )

        val subtitle = TextView(this).apply {
            textSize = 21f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }

        val description = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }

        val camera = Button(this)
        val voice = Button(this)
        val lessons = Button(this)
        val progress = Button(this)

        fun updateLanguage(position: Int) {
            when (position) {
                0 -> {
                    subtitle.text = "Bugun nimani o‘rganamiz?"
                    description.text = "Ko‘r, tushun va yangi bilimlarni o‘rgan."
                    camera.text = "📷 Kamera orqali o‘rganish"
                    voice.text = "🎙 Ovoz orqali savol berish"
                    lessons.text = "📚 Mening darslarim"
                    progress.text = "📊 Bilimlarim va natijalarim"
                }
                1 -> {
                    subtitle.text = "Что будем изучать сегодня?"
                    description.text = "Смотри, понимай и изучай новое."
                    camera.text = "📷 Обучение через камеру"
                    voice.text = "🎙 Задать вопрос голосом"
                    lessons.text = "📚 Мои уроки"
                    progress.text = "📊 Мои знания и результаты"
                }
                2 -> {
                    subtitle.text = "What shall we learn today?"
                    description.text = "See, understand and discover new things."
                    camera.text = "📷 Learn with camera"
                    voice.text = "🎙 Ask by voice"
                    lessons.text = "📚 My lessons"
                    progress.text = "📊 My progress"
                }
            }
        }

        root.addView(title)
        root.addView(languageSpinner)
        root.addView(subtitle)
        root.addView(description)
        root.addView(camera)
        root.addView(voice)
        root.addView(lessons)
        root.addView(progress)

        languageSpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    updateLanguage(position)
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}
            }

        updateLanguage(0)
        setContentView(root)
    }
}
