package com.example.hackaton

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.hackaton.databinding.SettingsActivityBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val HomeActivityButton = findViewById<ImageButton>(R.id.btn3)
        HomeActivityButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val ActivitiesActivityButton = findViewById<ImageButton>(R.id.SettingsActivityButton)
        ActivitiesActivityButton.setOnClickListener {
            val intent = Intent(this, ScrollingActivity::class.java)
            startActivity(intent)
   3     }

        val SettingsActivityButton = findViewById<ImageButton>(R.id.SetingsSetingsButton)
        SettingsActivityButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }



}