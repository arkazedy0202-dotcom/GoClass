package com.example.goclass

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. Sambungkan dengan desain layar Alarm
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)

        // ==========================================
        // 2. KENALKAN KOTLIN DENGAN ID TOMBOL DI XML
        // ==========================================
        // ID ini sudah disesuaikan persis dengan fragment_alarm.xml milikmu
        val tombolBuRiri = view.findViewById<View>(R.id.btnPanggilBuRiri)
        val tombolPakNaufal = view.findViewById<View>(R.id.btnPanggilPakNaufal)
        val tombolPakHarry = view.findViewById<View>(R.id.btnPanggilPakHarry)
        val tombolBuSiti = view.findViewById<View>(R.id.btnPanggilBuSiti)

        val tombolBuZuliani = view.findViewById<View>(R.id.btnPanggilBuZuliani)
        val tombolPakAmiruddin = view.findViewById<View>(R.id.btnPanggilPakAmiruddin)
        val tombolBuDerlina = view.findViewById<View>(R.id.btnPanggilBuDerlina)
        val tombolBuDessy = view.findViewById<View>(R.id.btnPanggilBuDessy)
        val tombolBuNurkamila = view.findViewById<View>(R.id.btnPanggilBuNurkamila)
        val tombolBuShinta = view.findViewById<View>(R.id.btnPanggilBuShinta)
        val tombolBuKurnia = view.findViewById<View>(R.id.btnPanggilBuKurnia)

        // ==========================================
        // 3. PASANG PERINTAH KLIK (Sesuai Username Baru di Database)
        // ==========================================
        tombolBuRiri?.setOnClickListener { panggilGuru("riri") }
        tombolPakNaufal?.setOnClickListener { panggilGuru("naufal") }
        tombolPakHarry?.setOnClickListener { panggilGuru("harry") }
        tombolBuSiti?.setOnClickListener { panggilGuru("siti") }

        tombolBuZuliani?.setOnClickListener { panggilGuru("zuliani") }
        tombolPakAmiruddin?.setOnClickListener { panggilGuru("amiruddin") }
        tombolBuDerlina?.setOnClickListener { panggilGuru("derlina") }
        tombolBuDessy?.setOnClickListener { panggilGuru("dessy") }
        tombolBuNurkamila?.setOnClickListener { panggilGuru("nurkamila") }
        tombolBuShinta?.setOnClickListener { panggilGuru("shinta") }
        tombolBuKurnia?.setOnClickListener { panggilGuru("kurnia") }

        return view
    }

    // ==========================================
    // 4. FUNGSI UNTUK MENGIRIM PANGGILAN KE FIREBASE
    // ==========================================
    private fun panggilGuru(usernameGuru: String) {
        // Ambil data siswa yang sedang login
        val sharedPref = requireActivity().getSharedPreferences("GoClassSession", Context.MODE_PRIVATE)
        val namaPemanggil = sharedPref.getString("NAMA", "Siswa") ?: "Siswa"
        val kelasPemanggil = sharedPref.getString("KELAS", "Kelas") ?: "Kelas"

        // Dapatkan waktu saat ini
        val sdfTanggal = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val sdfWaktu = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        val tanggalSekarang = sdfTanggal.format(Date())
        val waktuSekarang = sdfWaktu.format(Date())

        // Susun data yang akan dilempar ke Firebase
        val dataPanggilan = HashMap<String, String>()
        dataPanggilan["pemanggil"] = namaPemanggil
        dataPanggilan["kelas"] = kelasPemanggil
        dataPanggilan["tanggal"] = tanggalSekarang
        dataPanggilan["waktu_panggilan"] = waktuSekarang
        dataPanggilan["status"] = "Memanggil..."

        // Kirim ke Firebase (Ke laci Panggilan_Aktif milik guru yang dipilih)
        val dbRef = FirebaseDatabase.getInstance().getReference("Panggilan_Aktif").child(usernameGuru)
        dbRef.setValue(dataPanggilan).addOnSuccessListener {
            Toast.makeText(requireContext(), "Panggilan berhasil dikirim!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Gagal mengirim panggilan", Toast.LENGTH_SHORT).show()
        }
    }
}