package com.example.goclass

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RiwayatFragment : Fragment() {

    private lateinit var rvRiwayat: RecyclerView
    private lateinit var tvKosong: TextView
    private lateinit var riwayatList: ArrayList<Riwayat>
    private lateinit var adapter: RiwayatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_riwayat, container, false)

        rvRiwayat = view.findViewById(R.id.rvRiwayat)
        tvKosong = view.findViewById(R.id.tvRiwayatKosong)

        rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        rvRiwayat.setHasFixedSize(true)

        riwayatList = arrayListOf()
        adapter = RiwayatAdapter(riwayatList)
        rvRiwayat.adapter = adapter

        ambilDataOtomatis()

        return view
    }

    private fun ambilDataOtomatis() {
        // Mengambil semua kemungkinan nama kunci yang tersimpan saat Login
        val sharedPref = requireActivity().getSharedPreferences("GoClassSession", Context.MODE_PRIVATE)
        val sessionUsername = sharedPref.getString("USERNAME", "") ?: sharedPref.getString("username", "") ?: ""
        val sessionNama = sharedPref.getString("NAMA", "") ?: sharedPref.getString("nama", "") ?: ""

        // Sedot seluruh data dari laci "Riwayat" di Firebase
        val dbRef = FirebaseDatabase.getInstance().getReference("Riwayat")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                riwayatList.clear()

                if (snapshot.exists()) {
                    // Loop 1: Bongkar laci semua guru (bu_riri, pak_naufal, dll)
                    for (guruSnapshot in snapshot.children) {
                        val folderGuru = guruSnapshot.key.toString()

                        // Loop 2: Bongkar isi panggilan di dalam laci guru tersebut
                        for (dataSnapshot in guruSnapshot.children) {
                            val data = dataSnapshot.getValue(Riwayat::class.java)

                            if (data != null) {
                                // LOGIKA FILTERING OTOMATIS:
                                // 1. Jika yang login adalah GURU (nama folder sama dengan username yang login)
                                val iniRiwayatGuru = (folderGuru == sessionUsername)
                                // 2. Jika yang login adalah SISWA (nama pemanggil sama dengan nama siswa yang login)
                                val iniRiwayatSiswa = (data.pemanggil == sessionNama)

                                // Masukkan ke layar HANYA jika riwayat ini miliknya
                                if (iniRiwayatGuru || iniRiwayatSiswa) {
                                    riwayatList.add(data)
                                }
                            }
                        }
                    }

                    // Balik urutan agar riwayat paling baru muncul paling atas
                    riwayatList.reverse()

                    // Tampilkan ke layar
                    if (riwayatList.isNotEmpty()) {
                        adapter.notifyDataSetChanged()
                        rvRiwayat.visibility = View.VISIBLE
                        tvKosong.visibility = View.GONE
                    } else {
                        rvRiwayat.visibility = View.GONE
                        tvKosong.visibility = View.VISIBLE
                    }
                } else {
                    rvRiwayat.visibility = View.GONE
                    tvKosong.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Abaikan jika ada error jaringan
            }
        })
    }
}