package com.fera.paddie.auth.login

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fera.paddie.R
import com.fera.paddie.controller.UserController
import com.fera.paddie.model.TblUser
import com.fera.paddie.model.util.CONST
import com.fera.paddie.view.main.MainActivity
import com.fera.paddie.view.uploadToCloud.UploadToCloudActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date


class LoginFragment : Fragment(), AdapterLogin.AdapterLoginAction {
    private val TAG = "Test Credentials"
    private lateinit var v: View

    private lateinit var tiEdtUsername: TextInputEditText
    private lateinit var tiEdtPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView

    private lateinit var userController: UserController

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_login, container, false)

//        val intent = Intent(requireContext(), MainActivity::class.java)
//        startActivity(intent)

        initViews()
        addActionListeners()

        return v
    }

    private fun addActionListeners() {
        btnLogin.setOnClickListener {
            lifecycleScope.launch {
                loginUser()
            }
        }
        tvSignUp.setOnClickListener {
            clearInputFields()
            findNavController().navigate(R.id.navigateFromLoginToRegister)
        }
    }

    private fun login(uid: String) {
        val editor = requireContext().getSharedPreferences(CONST.SHARED_PREF_db, Context.MODE_PRIVATE).edit()
        editor.putString(CONST.SHARED_PREF_USER_ID, uid)
        editor.putBoolean(CONST.SHARED_PREF_IS_USER_SIGNED_IN, true)
        editor.apply()

        val intent = Intent(requireContext(), MainActivity::class.java)
        requireActivity().finish()
        startActivity(intent)
    }

    private fun loginUser() {
        val username = tiEdtUsername.text.toString().trim()
        val password = tiEdtPassword.text.toString().trim()

        if (username.isNotEmpty()) {
            mAuth.signInWithEmailAndPassword(username, password)
                .addOnSuccessListener { task ->
                    login(task.user!!.uid)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Incorrect Username or Password!", Toast.LENGTH_SHORT).show()
                }
        } else {
            val alert = AlertDialog.Builder(requireContext())
            alert.setMessage("Continue without creating Account?\nFor Demonstration purposes only.")
                .setTitle("No Account").setPositiveButton("Yes, Continue.",
                    DialogInterface.OnClickListener { dialog, which ->

                        requireActivity().finish()
                    })
                .setNeutralButton(
                    "No, Create Account",
                    DialogInterface.OnClickListener { dialog, which ->
                        findNavController().navigate(R.id.navigateFromLoginToRegister)
                    })
                .show()
        }



//        val userExists = userController.checkUser(username, password)


//        if (userExists == 1){
//            val intent = Intent(requireContext(), MainActivity::class.java)
//            startActivity(intent)
//            requireActivity().finish()
//        } else {
//            val alert = AlertDialog.Builder(requireContext())
//            alert.setMessage("Continue without creating Account?\nFor Demonstration purposes only.")
//                .setTitle("No Account")
//                .setPositiveButton("Yes, Continue.", DialogInterface.OnClickListener { dialog, which ->
//                    val user = User(username = "John doe", password = "12345", profilePic = "Demo", userJoinDate = Date().time)
//                    UserProperties.currentUser = user
//                    val intent = Intent(requireContext(), MainActivity::class.java)
//                    startActivity(intent)
//                    requireActivity().finish()
//                })
//                .setNeutralButton("No, Create Account", DialogInterface.OnClickListener { dialog, which ->
//                    findNavController().navigate(R.id.navigateFromLoginToRegister)
//                })
//                .show()
//        }
        clearInputFields()
    }

    private fun clearInputFields() {
        tiEdtPassword.setText("")
        tiEdtUsername.setText("")
    }

    private fun initViews() {
        mDBRef = FirebaseDatabase.getInstance().getReference()
        mAuth = FirebaseAuth.getInstance()
        userController = ViewModelProvider(this)[UserController::class.java]

        tiEdtUsername = v.findViewById(R.id.tiEdtUsername_login)
        tiEdtPassword = v.findViewById(R.id.tiEdtPassword_login)
        btnLogin = v.findViewById(R.id.btnLogin_login)
        tvSignUp = v.findViewById(R.id.tvSignUp_login)
    }

    override fun useUser(tblUser: TblUser) {
        mAuth.signInWithEmailAndPassword(tblUser.email!!, tblUser.password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){

                } else {

                }
            }
    }

    override fun removeUser(tblUser: TblUser): Boolean {
        var isSuccess = false
        mDBRef.child(CONST.fDB_DIR_USER).child(tblUser.uid!!).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    isSuccess = true
                } else {
                    isSuccess = false
                }
            }
        return isSuccess
    }
}

























