package com.example.goclass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButtonToggleGroup

class BerandaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beranda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toggleBlok = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleBlok)
        val blokAContent = view.findViewById<View>(R.id.blokAContent)
        val blokBContent = view.findViewById<View>(R.id.blokBContent)

        // Tukar tampilan sesuai blok yang dipilih
        toggleBlok.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnBlokA) {
                    blokAContent.visibility = View.VISIBLE
                    blokBContent.visibility = View.GONE
                } else {
                    blokAContent.visibility = View.GONE
                    blokBContent.visibility = View.VISIBLE
                }
            }
        }
    }
}