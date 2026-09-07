package com.example.goclass

import android.content.Context
import android.content.Intent
import android.os.Bundle
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

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 1. Ambil "KTP Digital" yang disimpan saat login
        val sharedPref = requireActivity().getSharedPreferences("GoClassSession", Context.MODE_PRIVATE)
        val nama = sharedPref.getString("NAMA", "Nama Tidak Ditemukan")
        val nis = sharedPref.getString("USERNAME", "-") ?: "-"
        val kelas = sharedPref.getString("EXTRA", "-")

        // 2. Suntikkan datanya ke TextView di layar
        view.findViewById<TextView>(R.id.tvNamaSiswa)?.text = nama
        view.findViewById<TextView>(R.id.tvKelasSiswa)?.text = kelas
        view.findViewById<TextView>(R.id.tvNisSiswa)?.text = nis

        // ==========================================
        // 3. LOGIKA MENU TITIK TIGA (POP-UP DROPDOWN)
        // ==========================================
        val btnMenu = view.findViewById<TextView>(R.id.btnMenuTitikTigaSiswa)
        btnMenu?.setOnClickListener {
            // Buat menu melayang (PopupMenu)
            val popup = android.widget.PopupMenu(requireContext(), btnMenu)

            // Tambahkan daftar pilihan
            popup.menu.add("Ganti Password")

            // Apa yang terjadi jika "Ganti Password" ditekan?
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Ganti Password") {
                    tampilkanDialogGantiPassword(nis)
                }
                true
            }

            // Tampilkan menu
            popup.show()
        }

        // ==========================================
        // 4. LOGIKA TOMBOL LOGOUT (Siswa)
        // ==========================================
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogoutSiswa)
        btnLogout?.setOnClickListener {
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
    // FUNGSI MEMBUAT POP-UP GANTI PASSWORD
    // ==========================================
    private fun tampilkanDialogGantiPassword(nisSiswa: String) {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(64, 32, 64, 32)

        val etPasswordLama = EditText(requireContext())
        etPasswordLama.hint = "Masukkan Password Lama"
        etPasswordLama.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(etPasswordLama)

        val etPasswordBaru = EditText(requireContext())
        etPasswordBaru.hint = "Masukkan Password Baru"
        etPasswordBaru.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(etPasswordBaru)

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

                if (passLama.isEmpty() || passBaru.isEmpty()) {
                    Toast.makeText(requireContext(), "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                buttonSimpan.isEnabled = false
                buttonSimpan.text = "Mengecek..."

                val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(nisSiswa)
                dbRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val passwordDiDatabase = snapshot.child("password").value.toString()

                        if (passLama == passwordDiDatabase) {
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