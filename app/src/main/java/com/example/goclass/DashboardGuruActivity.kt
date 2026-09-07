package com.example.goclass

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardGuruActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ==========================================
        // KEKUATAN MEMBANGUNKAN LAYAR & BUKA KUNCI
        // ==========================================
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        // ==========================================

        setContentView(R.layout.activity_dashboard_guru)

        // ==========================================
        // BARU: Cek Izin Otomatis Bertingkat
        // ==========================================
        cekDanMintaIzin()

        // ==========================================
        // NYALAKAN SATPAM LATAR BELAKANG DENGAN AMAN
        // ==========================================
        try {
            val serviceIntent = Intent(this, PemantauPanggilanService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // ==========================================

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationGuru)

        if (savedInstanceState == null) {
            replaceFragment(PanggilanFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_panggilan -> {
                    replaceFragment(PanggilanFragment())
                    true
                }
                R.id.nav_riwayat_guru -> {
                    replaceFragment(RiwayatFragment())
                    true
                }
                R.id.nav_profile_guru -> {
                    replaceFragment(ProfileGuruFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        if (isMotionEnabled()) {
            transaction.setCustomAnimations(R.anim.fade_in_short, R.anim.fade_out_short)
        }

        transaction
            .replace(R.id.fragmentContainerGuru, fragment)
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

    // ==========================================
    // FUNGSI PENGECEKAN IZIN OTOMATIS BERTINGKAT
    // ==========================================
    private fun cekDanMintaIzin() {
        // 1. Cek Izin "Tampilkan di Atas Aplikasi Lain" (Overlay)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                tampilkanDialogIzinOverlay()
                return // Hentikan di sini, minta izin overlay dulu
            }
        }

        // 2. Cek Izin "Abaikan Penghemat Baterai"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                tampilkanDialogIzinBaterai()
                return // Hentikan di sini, minta izin baterai dulu
            }
        }

        // 3. Cek Izin "Peluncuran Otomatis & Latar Belakang" (Khusus HP Tiongkok)
        // Karena sistem Android tidak bisa ngecek otomatis, kita gunakan ingatan (SharedPreferences)
        val prefs = getSharedPreferences("GoClassIzin", Context.MODE_PRIVATE)
        val sudahPernahMinta = prefs.getBoolean("SUDAH_MINTA_AUTOSTART", false)

        if (!sudahPernahMinta) {
            tampilkanDialogIzinAutoStart()
        }
    }

    private fun tampilkanDialogIzinOverlay() {
        AlertDialog.Builder(this)
            .setTitle("Izin Wajib Guru")
            .setMessage("Agar aplikasi GoClass bisa otomatis terbuka penuh saat ada panggilan, mohon izinkan fitur 'Tampilkan di Atas Aplikasi Lain'.")
            .setCancelable(false)
            .setPositiveButton("Beri Izin") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    try {
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                    } catch (e2: Exception) {}
                }
            }
            .setNegativeButton("Nanti", null)
            .show()
    }

    private fun tampilkanDialogIzinBaterai() {
        AlertDialog.Builder(this)
            .setTitle("Izin Latar Belakang")
            .setMessage("Sistem HP mendeteksi aplikasi bisa tertidur. Mohon pilih 'Tidak Dibatasi' atau abaikan penghemat baterai.")
            .setCancelable(false)
            .setPositiveButton("Beri Izin") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    try {
                        startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    } catch (e2: Exception) {}
                }
            }
            .setNegativeButton("Nanti", null)
            .show()
    }

    private fun tampilkanDialogIzinAutoStart() {
        AlertDialog.Builder(this)
            .setTitle("Langkah Terakhir!")
            .setMessage("Beberapa HP memblokir aplikasi secara ketat.\n\nMohon tekan 'Buka Pengaturan', lalu nyalakan saklar:\n\n1. Peluncuran Otomatis (Auto-Start)\n2. Izin Latar Belakang (Background Execution)")
            .setCancelable(false)
            .setPositiveButton("Buka Pengaturan") { _, _ ->
                // Catat ke ingatan agar besok-besok tidak muncul lagi
                getSharedPreferences("GoClassIzin", Context.MODE_PRIVATE)
                    .edit().putBoolean("SUDAH_MINTA_AUTOSTART", true).apply()

                try {
                    // Arahkan langsung ke halaman Info Aplikasi GoClass
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Sudah Izinkan") { _, _ ->
                // Jika user merasa sudah mengizinkan, jangan tampilkan lagi
                getSharedPreferences("GoClassIzin", Context.MODE_PRIVATE)
                    .edit().putBoolean("SUDAH_MINTA_AUTOSTART", true).apply()
            }
            .show()
    }
}
