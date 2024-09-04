package com.fera.paddie.view.uploadToCloud

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.model.TblNote


class AdapterUploadNoteList(private val context: Context, private var noteList: List<TblNote>, private val parentActivity: UploadToCloudActivity): RecyclerView.Adapter<AdapterUploadNoteList.MyViewHolder>() {
    interface NoteActivities {
        fun updateNote(tblNote: TblNote)
        fun deleteNote(id: String)
        fun updateFavourite(id: String, isFavourite: Boolean)
        suspend fun getNote(id: String): TblNote
    }

    private var allChecked = false

    class MyViewHolder(i: View): RecyclerView.ViewHolder(i) {
        val tvTitle: TextView = i.findViewById(R.id.tvNoteTitleListItem)
        val tvDate: TextView = i.findViewById(R.id.tvNoteDateListItem)
        val tvDesc: TextView = i.findViewById(R.id.tvNoteDescListItem)
        val ivDelete: ImageView = i.findViewById(R.id.ivDeleteNoteListItem)
//        val ivFavourite: ImageView = i.findViewById(R.id.ivFavouriteNoteListItem)
        val chkbxSelectBox: CheckBox = i.findViewById(R.id.chkbxSelectBox)
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
            tvDate.visibility = View.GONE

            ivDelete.setOnClickListener {
                parentActivity.deleteNote(noteList[position].pkNoteId!!)
            }

//            if(noteList[position].isFavourite){
//                ivFavourite.setImageResource(R.drawable.ic_favourite)
//            } else {
//                ivFavourite.setImageResource(R.drawable.ic_unfavourite)
//            }

//            ivFavourite.setOnClickListener {
//                val fav = !noteList[position].isFavourite
//
//                if(fav){
//                    ivFavourite.setImageResource(R.drawable.ic_favourite)
//                } else {
//                    ivFavourite.setImageResource(R.drawable.ic_unfavourite)
//                }
//
//                noteList[position].isFavourite = fav
//                parentActivity.updateFavourite(noteList[position].pkNoteTodoId!!, fav)
//            }

            if (allChecked){
                chkbxSelectBox.isChecked = true
            } else {
                chkbxSelectBox.isChecked = false
            }

            chkbxSelectBox.visibility = View.VISIBLE

            chkbxSelectBox.setOnClickListener {
                if (chkbxSelectBox.isChecked) {
                    chkbxSelectBox.isChecked = true
                    parentActivity.addToUploadList(noteList[position])
                } else {
                    chkbxSelectBox.isChecked = false
                    parentActivity.removeFromUploadList(noteList[position])
                }
            }

            itemView.setOnClickListener {
                // TODO: Open notes to be edited
//                CoroutineScope(Dispatchers.IO).launch {
//                    val tblNote = parentActivity.getNote(noteList[position].pkNoteTodoId)
//                    val bundle = Bundle().apply {
//                        putParcelable("tblNote", tblNote)
//                    }
//
//                }
                Toast.makeText(parentActivity, "Can't Edit notes here...", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun checkAll(check: Boolean){
        allChecked = check
        parentActivity.addToUploadList(noteList)
        notifyDataSetChanged()
    }
}





















