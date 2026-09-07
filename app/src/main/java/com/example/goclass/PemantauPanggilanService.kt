package com.example.goclass

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PemantauPanggilanService : Service() {

    // ==========================================
    // BARU: Kunci Anti-Kedip (Mencegah Infinite Loop)
    // ==========================================
    private var isMemantau = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @android.annotation.SuppressLint("ForegroundServiceType", "MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "GoClass_Guru_Channel"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId, "Pemantau Panggilan GoClass", NotificationManager.IMPORTANCE_LOW
                )
                val manager = getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }

            val notifIntent = Intent(this, DashboardGuruActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, notifIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notifikasi = NotificationCompat.Builder(this, channelId)
                .setContentTitle("GoClass Guru Aktif")
                .setContentText("Sistem bersiap menerima panggilan siswa...")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(1, notifikasi)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ==========================================
        // CEK KUNCI: Jangan pasang satpam ganda!
        // ==========================================
        if (!isMemantau) {
            mulaiPantauFirebase()
            isMemantau = true // Kunci pintunya agar tidak dipanggil berkali-kali
        }

        return START_STICKY
    }

    private fun mulaiPantauFirebase() {
        val sharedPref = getSharedPreferences("GoClassSession", Context.MODE_PRIVATE)
        val usernameGuru = sharedPref.getString("USERNAME", "") ?: ""

        if (usernameGuru.isEmpty()) return

        val dbPanggilanAktif = FirebaseDatabase.getInstance().getReference("Panggilan_Aktif").child(usernameGuru)

        dbPanggilanAktif.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val channelIdAlarm = "GoClass_Alarm_Channel"

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val channelAlarm = NotificationChannel(channelIdAlarm, "Alarm Panggilan Masuk", NotificationManager.IMPORTANCE_HIGH)
                            val manager = getSystemService(NotificationManager::class.java)
                            manager.createNotificationChannel(channelAlarm)
                        }

                        // Gunakan SINGLE_TOP agar layar tidak dimuat ulang jika sudah terbuka
                        val intentBukaLayar = Intent(this@PemantauPanggilanService, DashboardGuruActivity::class.java)
                        intentBukaLayar.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

                        val pendingIntentBuka = PendingIntent.getActivity(
                            this@PemantauPanggilanService, 2, intentBukaLayar,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        val notifDobrak = NotificationCompat.Builder(this@PemantauPanggilanService, channelIdAlarm)
                            .setContentTitle("PANGGILAN MASUK!")
                            .setContentText("Buka aplikasi sekarang.")
                            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_CALL)
                            .setFullScreenIntent(pendingIntentBuka, true)
                            .build()

                        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notifManager.notify(99, notifDobrak)

                        // ==========================================
                        // PAKSA BUKA (Dengan Kunci SINGLE_TOP)
                        // ==========================================
                        try {
                            val paksaBuka = Intent(this@PemantauPanggilanService, DashboardGuruActivity::class.java)
                            paksaBuka.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(paksaBuka)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}