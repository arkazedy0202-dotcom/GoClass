package com.example.goclass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ==========================================
        // 1. CEK STATUS AUTO-LOGIN
        // ==========================================
        val sharedPref = getSharedPreferences("GoClassSession", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val savedRole = sharedPref.getString("ROLE", "")

        // Jika sebelumnya sudah berhasil login, langsung lompat ke Dashboard!
        if (isLoggedIn) {
            if (savedRole == "siswa") {
                startActivity(Intent(this, DashboardSiswaActivity::class.java))
                finish()
                return
            } else if (savedRole == "guru") {
                startActivity(Intent(this, DashboardGuruActivity::class.java))
                finish()
                return
            }
        }

        // Jika belum login sama sekali, tampilkan layar login
        setContentView(R.layout.activity_login)

        // ==========================================
        // 2. HUBUNGKAN KOTLIN DENGAN XML
        // ==========================================
        val kolomUsername = findViewById<TextInputEditText>(R.id.etInput)
        val kolomPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val tilInput = findViewById<TextInputLayout>(R.id.tilInput)
        val toggleRole = findViewById<MaterialButtonToggleGroup>(R.id.toggleRole)
        val tombolLogin = findViewById<Button>(R.id.btnLogin)

        // Ganti teks hint sesuai tombol yang dipilih
        toggleRole.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                tilInput.hint = if (checkedId == R.id.btnGuru) "Username Guru" else "NIS Siswa"
            }
        }

        // ==========================================
        // 3. AKSI SAAT TOMBOL LOGIN DITEKAN
        // ==========================================
        tombolLogin.setOnClickListener {
            val usernameDiketik = kolomUsername.text.toString().trim()
            val passwordDiketik = kolomPassword.text.toString().trim()

            if (usernameDiketik.isEmpty() || passwordDiketik.isEmpty()) {
                Toast.makeText(this, "NIS/Username dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Catat tombol mana yang lagi dipilih user
            val roleDipilih = if (toggleRole.checkedButtonId == R.id.btnGuru) "guru" else "siswa"

            tombolLogin.isEnabled = false
            tombolLogin.text = "WELCOME"

            val database = FirebaseDatabase.getInstance().getReference("Users")

            database.child(usernameDiketik).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val dbPassword = snapshot.child("password").value?.toString() ?: ""
                    val role = snapshot.child("role").value.toString()
                    val nama = snapshot.child("nama").value.toString()
                    val kelas = snapshot.child("kelas").value?.toString() ?: ""
                    val mapel = snapshot.child("mapel").value?.toString() ?: ""
                    val extraInfo = if (role == "siswa") kelas else mapel

                    // Cek kecocokan password
                    if (dbPassword != passwordDiketik) {
                        Toast.makeText(this, "Password salah!", Toast.LENGTH_SHORT).show()
                        tombolLogin.isEnabled = true
                        tombolLogin.text = "Log in"
                        return@addOnSuccessListener
                    }

                    // Tolak kalau tombol yang dipilih tidak cocok role asli di database
                    if (role != roleDipilih) {
                        Toast.makeText(
                            this,
                            "Akun ini terdaftar sebagai ${role.uppercase()}, bukan ${roleDipilih.uppercase()}. Silakan pilih tombol yang sesuai.",
                            Toast.LENGTH_LONG
                        ).show()
                        tombolLogin.isEnabled = true
                        tombolLogin.text = "Log in"
                        return@addOnSuccessListener
                    }

                    // SIMPAN DATA KE MEMORI HP (OTOMATIS REMEMBER ME)
                    val editor = sharedPref.edit()
                    editor.putString("USERNAME", usernameDiketik)
                    editor.putString("ROLE", role)
                    editor.putString("NAMA", nama)
                    editor.putString("EXTRA", extraInfo)

                    // BARU: Otomatis diset menjadi true tanpa butuh centang!
                    editor.putBoolean("IS_LOGGED_IN", true)
                    editor.apply()

                    if (role == "guru") {
                        Toast.makeText(this, "Selamat datang, Bapak/Ibu $nama!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DashboardGuruActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Selamat datang, $nama!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DashboardSiswaActivity::class.java))
                        finish()
                    }

                } else {
                    Toast.makeText(this, "Akun tidak ditemukan atau salah ketik!", Toast.LENGTH_SHORT).show()
                    tombolLogin.isEnabled = true
                    tombolLogin.text = "Log in"
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal terhubung ke server. Cek internetmu!", Toast.LENGTH_SHORT).show()
                tombolLogin.isEnabled = true
                tombolLogin.text = "Log in"
            }
        }
    }
}