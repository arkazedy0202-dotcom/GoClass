package com.example.goclass

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivLogo = findViewById<ImageView>(R.id.ivLogo)
        val tvTagline = findViewById<TextView>(R.id.tvTagline)

        // --- SETTING AWAL SEBELUM ANIMASI (DISEMBUNYIKAN) ---
        // Logo ditaruh agak ke bawah, dikecilkan, dan transparan
        ivLogo.alpha = 0f
        ivLogo.scaleX = 0.5f
        ivLogo.scaleY = 0.5f
        ivLogo.translationY = 150f

        // Tagline ditaruh di bawah dan transparan
        tvTagline.alpha = 0f
        tvTagline.translationY = 50f

        // --- MENJALANKAN ANIMASI BERUNTUN ---
        lifecycleScope.launch {
            // FASE 1: Logo melayang naik dan membesar melebihi ukuran asli
            ivLogo.animate()
                .alpha(1f)
                .scaleX(1.1f) // Membesar jadi 110%
                .scaleY(1.1f)
                .translationY(0f) // Naik ke posisi tengah
                .setDuration(800)
                .setInterpolator(DecelerateInterpolator()) // Gerakan melambat di ujung (mulus)
                .start()

            delay(800) // Tunggu logo selesai naik

            // FASE 2: Efek Denyut (Pulse) logo mengecil ke ukuran normal (100%)
            ivLogo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            // FASE 3: Tagline melompat masuk dari bawah secara bersamaan
            tvTagline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(OvershootInterpolator()) // Efek mantul kekinian
                .start()

            // Tahan layar selama 2000 detik agar user bisa melihat hasilnya
            delay(2000)

            // Pindah ke Halaman Login
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}