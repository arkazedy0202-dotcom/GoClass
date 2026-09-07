package com.example.goclass

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase

class ProfileGuruFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_guru, container, false)

        // 1. Ambil "KTP Digital" yang disimpan saat login
        val sharedPref = requireActivity().getSharedPreferences("GoClassSession", Context.MODE_PRIVATE)
        val nama = sharedPref.getString("NAMA", "Nama Tidak Ditemukan")
        val username = sharedPref.getString("USERNAME", "-") ?: "-"
        val mapel = sharedPref.getString("EXTRA", "-")

        // 2. Suntikkan datanya ke TextView di layar
        view.findViewById<TextView>(R.id.tvNamaGuru).text = nama
        view.findViewById<TextView>(R.id.tvMapelGuru).text = mapel
        view.findViewById<TextView>(R.id.tvNipGuru).text = username

        // ==========================================
        // 3. LOGIKA MENU TITIK TIGA (POP-UP DROPDOWN)
        // ==========================================
        val btnMenu = view.findViewById<TextView>(R.id.btnMenuTitikTiga)
        btnMenu.setOnClickListener {
            // Buat menu melayang (PopupMenu)
            val popup = android.widget.PopupMenu(requireContext(), btnMenu)

            // Tambahkan daftar pilihan ke dalamnya
            popup.menu.add("Ganti Password")
            popup.menu.add("Perbaiki Izin Auto-Buka")

            // Apa yang terjadi jika pilihan diklik?
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Ganti Password" -> {
                        tampilkanDialogGantiPassword(username)
                    }
                    "Perbaiki Izin Auto-Buka" -> {
                        bukaPengaturanIzin()
                    }
                }
                true
            }

            // Tampilkan menu!
            popup.show()
        }

        // ==========================================
        // 4. LOGIKA TOMBOL LOGOUT
        // ==========================================
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogoutGuru)
        btnLogout.setOnClickListener {
            // Matikan Satpam Latar Belakang
            val matikanSatpam = Intent(requireContext(), PemantauPanggilanService::class.java)
            requireContext().stopService(matikanSatpam)

            // Hapus KTP Digital
            sharedPref.edit().clear().apply()

            // Tendang kembali ke layar Login
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        return view
    }

    // ==========================================
    // FUNGSI MELEMPAR USER KE PENGATURAN HP
    // ==========================================
    private fun bukaPengaturanIzin() {
        try {
            // 1. Lempar langsung ke halaman izin "Tampilkan di Atas Aplikasi Lain" (Overlay)
            val intentOverlay = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intentOverlay.data = Uri.parse("package:" + requireContext().packageName)
            startActivity(intentOverlay)
            Toast.makeText(requireContext(), "Silakan IZINKAN GoClass agar bisa membuka layar otomatis!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // 2. Jika HP menolak, lempar ke halaman Info Aplikasi
            val intentInfo = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intentInfo.data = Uri.parse("package:" + requireContext().packageName)
            startActivity(intentInfo)
            Toast.makeText(requireContext(), "Silakan cari menu Izin / Baterai, lalu izinkan semuanya.", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // FUNGSI MEMBUAT POP-UP GANTI PASSWORD
    // ==========================================
    private fun tampilkanDialogGantiPassword(usernameGuru: String) {
        // Buat wadah untuk kotak inputan
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(64, 32, 64, 32)

        // Buat kolom input Password Lama
        val etPasswordLama = EditText(requireContext())
        etPasswordLama.hint = "Masukkan Password Lama"
        etPasswordLama.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(etPasswordLama)

        // Buat kolom input Password Baru
        val etPasswordBaru = EditText(requireContext())
        etPasswordBaru.hint = "Masukkan Password Baru"
        etPasswordBaru.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(etPasswordBaru)

        // Rakit pop-up dialognya
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Ganti Password")
            .setView(layout)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val buttonSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            buttonSimpan.setOnClickListener {
                val passLama = etPasswordLama.text.toString().trim()
                val passBaru = etPasswordBaru.text.toString().trim()

                // Cek apakah ada yang kosong
                if (passLama.isEmpty() || passBaru.isEmpty()) {
                    Toast.makeText(requireContext(), "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                buttonSimpan.isEnabled = false
                buttonSimpan.text = "Mengecek..."

                // Hubungi Firebase untuk mengecek password lama
                val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(usernameGuru)
                dbRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val passwordDiDatabase = snapshot.child("password").value.toString()

                        if (passLama == passwordDiDatabase) {
                            // Jika password lama cocok, timpa dengan password baru!
                            dbRef.child("password").setValue(passBaru).addOnSuccessListener {
                                Toast.makeText(requireContext(), "Password berhasil diubah!", Toast.LENGTH_LONG).show()
                                dialog.dismiss()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Password lama salah!", Toast.LENGTH_SHORT).show()
                            buttonSimpan.isEnabled = true
                            buttonSimpan.text = "Simpan"
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal terhubung ke server!", Toast.LENGTH_SHORT).show()
                    buttonSimpan.isEnabled = true
                    buttonSimpan.text = "Simpan"
                }
            }
        }

        dialog.show()
    }
}