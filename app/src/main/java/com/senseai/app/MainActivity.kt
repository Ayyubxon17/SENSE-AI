package com.senseai.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(28, 35, 28, 35)
        root.setBackgroundColor(Color.rgb(12, 18, 32))

        val title = TextView(this)
        title.text = "SENSE AI"
        title.textSize = 36f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        val subtitle = TextView(this)
        subtitle.text = "Atrofingdagi dunyoni ko‘r, tushun va o‘rgan"
        subtitle.textSize = 17f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER

        val camera = Button(this)
        camera.text = "📷  KAMERA ORQALI O‘RGANISH"

        val voice = Button(this)
        voice.text = "🎙  OVOZ ORQALI SAVOL BERISH"

        val lessons = Button(this)
        lessons.text = "📚  MENING DARSLARIM"

        root.addView(title)
        root.addView(subtitle)
        root.addView(camera)
        root.addView(voice)
        root.addView(lessons)

        setContentView(root)
    }
}
