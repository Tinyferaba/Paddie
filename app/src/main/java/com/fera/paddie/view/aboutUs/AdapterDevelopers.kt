package com.fera.paddie.view.aboutUs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fera.paddie.R
import com.fera.paddie.model.TblDevelopers
import com.google.android.material.imageview.ShapeableImageView

class AdapterDevelopers(private val context: Context, private val developers: List<TblDevelopers>): RecyclerView.Adapter<AdapterDevelopers.DeveloperViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeveloperViewHolder {
        return DeveloperViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_developers, parent, false))
    }

    override fun getItemCount(): Int {
        return developers.size
    }

    override fun onBindViewHolder(holder: DeveloperViewHolder, position: Int) {
        holder.apply {
            Glide.with(context)
                .load(developers[position].profilePhoto)
                .placeholder(R.drawable.kamake)
                .centerCrop()
                .into(sIvDevPhoto)

            val fullName = developers[position].firstName?.plus(" ${developers[position].lastName}")

            tvDevName.text = fullName
            tvDevID.text = developers[position].stdId
            tvDevEmail.text = developers[position].email
        }
    }

    class DeveloperViewHolder(v:View): RecyclerView.ViewHolder(v) {
        val sIvDevPhoto = v.findViewById<ShapeableImageView>(R.id.sIvDeveloperPhoto_listItem)
        val tvDevName = v.findViewById<TextView>(R.id.tvDeveloperName_listItem)
        val tvDevID = v.findViewById<TextView>(R.id.tvDeveloperId_listItem)
        val tvDevEmail = v.findViewById<TextView>(R.id.tvDeveloperEmail_listItem)
    }
}