package com.fera.paddie.auth.login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.model.TblUser

class AdapterLogin(private val users: List<TblUser>, private val context: Context, private val parentFragment: LoginFragment): RecyclerView.Adapter<AdapterLogin.LoginUserViewHolder>() {
    interface AdapterLoginAction {
        //TODO: User room to implement these
        fun useUser(tblUser: TblUser)
        fun removeUser(tblUser: TblUser): Boolean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoginUserViewHolder {
        return LoginUserViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_user, parent, false))
    }

    override fun onBindViewHolder(holder: LoginUserViewHolder, position: Int) {
        holder.apply {
//            ivUserPic.setImageURI()
            val firstname = users[position].firstName
            val lastname = users[position].firstName
            tvUsername.text = firstname.plus(" $lastname")

            ivDelete.setOnClickListener {
                val userRemoved = parentFragment.removeUser(users[position])
                if (userRemoved)
                    notifyItemChanged(position)
            }

            itemView.setOnClickListener {
                parentFragment.useUser(users[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    class LoginUserViewHolder(i:View): RecyclerView.ViewHolder(i) {
        val ivUserPic: ImageView = i.findViewById(R.id.sIvProfilePhoto_listItem)
        val tvUsername: TextView = i.findViewById(R.id.tvUsername_listItem)
        val ivDelete: ImageView = i.findViewById(R.id.ivDeleteUser_listItem)
    }
}