package com.example.aplikasipresensi.data.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasipresensi.R
import com.example.aplikasipresensi.ui.riwayat.Riwayat

class RecyclerAdapterRiwayat(list: MutableList<Riwayat>, context: Context):
    RecyclerView.Adapter<RecyclerAdapterRiwayat.ViewHolder>() {
    var dataList: MutableList<Riwayat>

    private var onItemClickCallback: OnItemClickCallback? = null
    var ctx: Context
    fun setOnClickCallback(onItemClickCallback: OnItemClickCallback?) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View =
            layoutInflater.inflate(R.layout.row_item_riwayat, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.tanggal.text = (String.format("%02d", position + 1))

        if(dataList[position].jamKeluar != ""){
            holder.jamKeluar.text = dataList[position].jamKeluar
        }else {
            holder.jamKeluar.text = "-"
        }
        if(dataList[position].jamMasuk != ""){
            holder.jamMasuk.text = dataList[position].jamMasuk
        }else {
            holder.jamMasuk.text = "-"
        }

        holder.itemView.setOnClickListener {
            onItemClickCallback!!.onItemClick(
                dataList[position],
                position
            )
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    init {
        dataList = list
        ctx = context
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tanggal: TextView
        var jamMasuk : TextView
        var jamKeluar : TextView
        init {
            tanggal = itemView.findViewById(R.id.tv_tanggal)
            jamMasuk = itemView.findViewById(R.id.tv_jam_masuk)
            jamKeluar = itemView.findViewById(R.id.tv_jam_keluar)
        }
    }

    interface OnItemClickCallback {
        fun onItemClick(item: Riwayat?, position: Int)
    }
}