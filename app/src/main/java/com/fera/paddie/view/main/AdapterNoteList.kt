package com.fera.paddie.view.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.model.TblNote
import com.fera.paddie.util.DateFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AdapterNoteList(private val context: Context, private var noteList: List<TblNote>, private val fragment: MainFragment): RecyclerView.Adapter<AdapterNoteList.MyViewHolder>() {
    interface NoteActivities {
        fun updateNote(tblNote: TblNote)
        fun deleteNote(id: Int)
        fun updateFavourite(id: Int, isFavourite: Boolean)
        suspend fun getNote(id: Int): TblNote
    }

    class MyViewHolder(i: View): RecyclerView.ViewHolder(i) {
        val tvTitle: TextView = i.findViewById(R.id.tvNoteTitleListItem)
        val tvDate: TextView = i.findViewById(R.id.tvNoteDateListItem)
        val tvDesc: TextView = i.findViewById(R.id.tvNoteDescListItem)
        val ivDelete: ImageView = i.findViewById(R.id.ivDeleteNoteListItem)
        val ivFavourite: ImageView = i.findViewById(R.id.ivFavouriteNoteListItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_note, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    fun updateNoteList(list: List<TblNote>){
        noteList = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            tvTitle.text = noteList[position].title
            tvDesc.text = noteList[position].description
            tvDate.text = DateFormatter.formatDate(noteList[position].dateModified)

            ivDelete.setOnClickListener {
                fragment.deleteNote(noteList[position].pkNoteTodoId)
            }

            if(noteList[position].isFavourite){
                ivFavourite.setImageResource(R.drawable.ic_favourite)
            } else {
                ivFavourite.setImageResource(R.drawable.ic_unfavourite)
            }

            ivFavourite.setOnClickListener {
                val fav = !noteList[position].isFavourite

                if(fav){
                    ivFavourite.setImageResource(R.drawable.ic_favourite)
                } else {
                    ivFavourite.setImageResource(R.drawable.ic_unfavourite)
                }

                noteList[position].isFavourite = fav
                fragment.updateFavourite(noteList[position].pkNoteTodoId, fav)
            }

            itemView.setOnLongClickListener {
                ContextCompat.getColor(itemView.context, R.color.light_green)
                itemView.scaleX = 1.1f
                itemView.scaleY = 1.1f
                true
            }

            itemView.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val tblNote = fragment.getNote(noteList[position].pkNoteTodoId)
                    val bundle = Bundle().apply {
                        putParcelable("tblNote", tblNote)
                    }

                    fragment.findNavController().navigate(R.id.addNoteFragment, bundle)
                }
            }
        }
    }
}





















