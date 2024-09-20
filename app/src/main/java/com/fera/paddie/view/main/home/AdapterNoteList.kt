package com.fera.paddie.view.main.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.model.TblNote
import com.fera.paddie.util.DateFormatter
import java.util.Date


class AdapterNoteList(private val context: Context, var noteList: List<TblNote>, val parentAct: MainActivity): RecyclerView.Adapter<AdapterNoteList.MyViewHolder>() {
    private var showCheckBox = false
    private var allChecked = false

    interface NoteActivities {
        fun updateNote(tblNote: TblNote)
        fun deleteNote(id: Int)
        fun updateFavourite(id: Int, favourite: Boolean)
        fun navigateToAddNoteFragment(id: Int)
        fun addToDeleteList(noteList: List<TblNote>)
        fun clearDeleteList()
        fun changeMode(mode: MODE)
        fun removeFromDeletedList(tblNote: TblNote)
        fun addToDeleteList(note: TblNote)
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
            var desc = noteList[position].description
            desc?.let {
                if (it.length > 30)
                    desc = it.substring(0, 28)
            }
            tvDesc.text = desc
            tvDate.text = DateFormatter.formatDate(Date(noteList[position].dateModified))

//            ivDelete.setOnClickListener {
//                parentAct.deleteNote(noteList[position].pkNoteId!!)
//            }

            if(noteList[position].favourite){
                ivFavourite.setImageResource(R.drawable.ic_favourite)
            } else {
                ivFavourite.setImageResource(R.drawable.ic_unfavourite)
            }

            if (showCheckBox){
                checkBox.visibility = View.VISIBLE
                if (allChecked){
                    checkBox.isChecked = true
                } else {
                    checkBox.isChecked = false
                }
            } else {
                checkBox.visibility = View.GONE
            }

            ivFavourite.setOnClickListener {
                val fav = !noteList[position].favourite

                if(fav){
                    ivFavourite.setImageResource(R.drawable.ic_favourite)
                } else {
                    ivFavourite.setImageResource(R.drawable.ic_unfavourite)
                }

                noteList[position].favourite = fav
                parentAct.updateFavourite(noteList[position].pkNoteId!!, fav)

                notifyItemChanged(position)
            }

            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    checkBox.isChecked = true
                    parentAct.addToDeleteList(noteList[position])
                } else {
                    checkBox.isChecked = false
                    parentAct.removeFromDeletedList(noteList[position])
                }
            }

            //todo: Complete this functionality
            itemView.setOnLongClickListener {
                val color = ContextCompat.getColor(itemView.context, R.color.light_green)

                parentAct.changeMode(MODE.DELETE)
                showCheckBox = true
                notifyDataSetChanged()
                checkBox.isChecked = true
                true
            }

            itemView.setOnClickListener {
                parentAct.navigateToAddNoteFragment(noteList[position].pkNoteId!!)
            }
        }
    }

    fun checkAll(check: Boolean){
        allChecked = check
        if (check){
            parentAct.addToDeleteList(noteList)
        } else {
            parentAct.clearDeleteList()
        }
        notifyDataSetChanged()
    }

    fun hideCheckBox(){
        showCheckBox = false
        notifyDataSetChanged()
    }

    class MyViewHolder(i: View): RecyclerView.ViewHolder(i) {
        val tvTitle: TextView = i.findViewById(R.id.tvNoteTitleListItem)
        val tvDate: TextView = i.findViewById(R.id.tvNoteDateListItem)
        val tvDesc: TextView = i.findViewById(R.id.tvNoteDescListItem)
        //        val ivDelete: ImageView = i.findViewById(R.id.ivDeleteNoteListItem)
        val ivFavourite: ImageView = i.findViewById(R.id.ivFavouriteNoteListItem)
        val checkBox: CheckBox = i.findViewById(R.id.chkbxSelectBox_listItem)
    }
}





















