package com.example.goclass

import android.content.Context
import android.content.Intent // Import tambahan untuk membuka aplikasi
import android.media.AudioManager // Mesin pengatur volume
import android.media.MediaPlayer // Mesin pemutar musik
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PanggilanFragment : Fragment() {

    // Siapkan pemutar suara di luar agar bisa dimatikan nanti
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_panggilan, container, false)

        val sharedPref = requireActivity().getSharedPreferences("GoClassSession", Context.MODE_PRIVATE)
        val usernameGuru = sharedPref.getString("USERNAME", "") ?: ""

        val labelMenunggu = view.findViewById<TextView>(R.id.tvLabelMenunggu)
        val kartuPanggilan = view.findViewById<CardView>(R.id.cvKartuPanggilan)
        val teksKelas = view.findViewById<TextView>(R.id.tvKelasPemanggil)
        val teksWaktu = view.findViewById<TextView>(R.id.tvWaktuPanggilan)
        val tombolOtw = view.findViewById<MaterialButton>(R.id.btnOtw)

        val dbPanggilanAktif = FirebaseDatabase.getInstance().getReference("Panggilan_Aktif").child(usernameGuru)
        val dbRiwayat = FirebaseDatabase.getInstance().getReference("Riwayat").child(usernameGuru)

        dbPanggilanAktif.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val pemanggil = snapshot.child("pemanggil").value.toString()
                    val kelas = snapshot.child("kelas").value.toString()
                    val waktu = snapshot.child("waktu").value.toString()

                    teksKelas.text = "$kelas ($pemanggil)"
                    teksWaktu.text = "Waktu: $waktu (Baru Saja)"

                    // ==========================================
                    // LOGIKA ALARM FULL VOLUME & NON-STOP
                    // ==========================================
                    if (kartuPanggilan.visibility == View.GONE) {
                        try {
                            // 1. Bajak volume HP dan paksa ke maksimal (100%)
                            val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)

                            // 2. Putar suara alarm custom milikmu secara berulang (Looping)
                            if (mediaPlayer == null) {
                                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.tingg) // Pastikan nama file cocok
                                mediaPlayer?.isLooping = true // Suara tidak akan berhenti
                                mediaPlayer?.start()

                                // ==========================================
                                // 3. PELATUK MEMAKSA APLIKASI BUKA SENDIRI
                                // ==========================================

                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    labelMenunggu.visibility = View.VISIBLE
                    kartuPanggilan.visibility = View.VISIBLE

                    // ==========================================
                    // SAAT GURU KLIK OTW -> MATIKAN ALARM!
                    // ==========================================
                    tombolOtw.setOnClickListener {
                        Toast.makeText(requireContext(), "Memproses...", Toast.LENGTH_SHORT).show()

                        // Hentikan suara dan hancurkan pemutar musiknya
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null

                        val tanggalHariIni = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
                        val idUnik = dbRiwayat.push().key ?: "riwayat_baru"

                        val dataRiwayat = mapOf(
                            "pemanggil" to pemanggil,
                            "kelas" to kelas,
                            "tanggal" to tanggalHariIni,
                            "waktu_panggilan" to waktu,
                            "status" to "Selesai / Diterima"
                        )

                        dbRiwayat.child(idUnik).setValue(dataRiwayat).addOnSuccessListener {
                            dbPanggilanAktif.removeValue()
                            Toast.makeText(requireContext(), "Panggilan diterima! Selamat mengajar.", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    labelMenunggu.visibility = View.GONE
                    kartuPanggilan.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        return view
    }

    // PENTING: Jika aplikasi ditutup paksa saat alarm masih bunyi, matikan suaranya!
    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}