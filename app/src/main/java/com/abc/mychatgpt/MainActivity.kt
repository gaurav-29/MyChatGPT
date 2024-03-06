package com.abc.mychatgpt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.abc.mychatgpt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_toolbar))

        binding.chatLL.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        binding.generateImageLL.setOnClickListener {
            startActivity(Intent(this, GenerateImageActivity::class.java))
        }
        binding.translateLL.setOnClickListener {
            startActivity(Intent(this, TranslateActivity::class.java))
        }
    }
}