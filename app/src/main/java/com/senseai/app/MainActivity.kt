
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
        root.setPadding(24, 45, 24, 24)
        root.setBackgroundColor(Color.WHITE)

        val title = TextView(this).apply {
            text = "SENSE AI"
            textSize = 30f
            setTextColor(Color.rgb(25, 75, 160))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Bugun nimani o‘rganamiz?"
            textSize = 21f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }

        val description = TextView(this).apply {
            text = "Kamera, ovoz va sun’iy intellekt yordamida yangi bilimlarni kashf eting."
            textSize = 15f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(description)

        fun addMenuButton(label: String) {
            val button = Button(this).apply {
                text = label
                textSize = 15f
                isAllCaps = false
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.rgb(35, 105, 210))
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                62
            )
            params.setMargins(0, 12, 0, 4)
            root.addView(button, params)
        }

        addMenuButton("📷  Kamera orqali o‘rganish")
        addMenuButton("🎙  Ovoz orqali savol berish")
        addMenuButton("📚  Mening darslarim")
        addMenuButton("📊  Bilimlarim va natijalarim")

        setContentView(root)
    }
}
