package com.example.goclass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RiwayatAdapter(private val listRiwayat: ArrayList<Riwayat>) : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUtama: TextView = itemView.findViewById(R.id.tvRiwayatUtama)
        val tvKeterangan: TextView = itemView.findViewById(R.id.tvRiwayatKeterangan)
        val tvWaktu: TextView = itemView.findViewById(R.id.tvRiwayatWaktu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val riwayat = listRiwayat[position]

        // Presentation-only fallback: never expose empty or literal "null" values.
        fun String?.forDisplay(fallback: String): String {
            return this?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
                ?: fallback
        }

        val pemanggil = riwayat.pemanggil.forDisplay("Siswa")
        val kelas = riwayat.kelas.forDisplay("Kelas belum tercatat")
        val status = riwayat.status.forDisplay("Selesai / Diterima")
        val waktu = listOf(riwayat.tanggal, riwayat.waktu_panggilan)
            .mapNotNull { value ->
                value?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
            }
            .joinToString(" • ")
            .ifBlank { "Waktu belum tercatat" }

        holder.tvUtama.text = "$kelas · $pemanggil"
        holder.tvKeterangan.text = status
        holder.tvWaktu.text = waktu
    }

    override fun getItemCount(): Int {
        return listRiwayat.size
    }
}
