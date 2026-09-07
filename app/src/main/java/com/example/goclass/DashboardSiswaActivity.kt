package com.example.goclass

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardSiswaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_siswa)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // 1. Tampilkan Jadwal Pelajaran (Beranda) saat pertama kali buka
        if (savedInstanceState == null) {
            replaceFragment(BerandaFragment())
        }

        // 2. Fungsi untuk klik menu bawah
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_beranda -> {
                    replaceFragment(BerandaFragment())
                    true
                }
                R.id.nav_alarm -> {
                    // SEKARANG MEMBUKA HALAMAN ALARM
                    replaceFragment(AlarmFragment())
                    true
                }
                R.id.nav_riwayat -> {
                        // MEMBUKA HALAMAN RIWAYAT
                        replaceFragment(RiwayatFragment())
                        true

                }
                R.id.nav_profile -> {
                    // Nanti kita isi dengan ProfileFragment
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Fungsi untuk menempelkan Fragment ke tengah layar
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        if (isMotionEnabled()) {
            transaction.setCustomAnimations(R.anim.fade_in_short, R.anim.fade_out_short)
        }

        transaction
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun isMotionEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ValueAnimator.areAnimatorsEnabled()
        } else {
            Settings.Global.getFloat(
                contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) > 0f
        }
    }
}
