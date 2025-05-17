package com.example.hackaton

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.example.hackaton.databinding.SettingsActivityBinding
import org.w3c.dom.Text


class time_per_app_activity : AppCompatActivity() {
    val appUsageTimeManager = AppUsageTimeManager(this)
    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(R.layout.time_activity)


        val HomeActivityButton = findViewById<ImageButton>(R.id.HomeTimeButton)
        HomeActivityButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val settingsActivityButton = findViewById<ImageButton>(R.id.TimeSettingsButton)
        settingsActivityButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val ScrollingActivityButton = findViewById<ImageButton>(R.id.rightActivity)
        ScrollingActivityButton.setOnClickListener {
            val intent = Intent(this, ScrollingActivity::class.java)
            startActivity(intent)
        }

        val TimeLabel = findViewById<TextView>(R.id.textViewMinutes)
        TimeLabel.text = appUsageTimeManager.getAppUsageMinutes("com.zhiliaoapp.musically").toString()
        val TimeLabel3 = findViewById<TextView>(R.id.textViewMinutes2)
        TimeLabel3.text = appUsageTimeManager.getAppUsageMinutes("com.facebook.katana").toString()
        val TImeLabel1 = findViewById<TextView>(R.id.textViewAppName)
        TImeLabel1.text = "TikTok"

    }

}