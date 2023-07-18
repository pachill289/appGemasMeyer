package com.example.gemasmeyerapp_ver2.Views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gemasmeyerapp_ver2.databinding.ActivityPagoBinding

class PagoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPagoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideStatusBar()
    }
    private fun hideStatusBar() {
        supportActionBar?.hide()
    }
}