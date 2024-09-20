package com.fera.paddie.auth.signup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fera.paddie.R
import com.fera.paddie.controller.UserController
import com.fera.paddie.model.TblUser
import com.fera.paddie.util.CONST
import com.fera.paddie.view.uploadToCloud.UploadToCloudActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {
    private val TAG = "SignUpFragment"
    //######### VALUES #########//

    //######### VIEWS #########//
    private lateinit var v: View
    private lateinit var tvLogin: TextView
    private lateinit var sIvProfilePhoto: ShapeableImageView
    private lateinit var btnAddProfilePic: Button
    private var uriProfilePhoto: Uri? = null
    private lateinit var tiEdtFirstName: TextInputEditText
    private lateinit var tiEdtMiddleName: TextInputEditText
    private lateinit var tiEdtLastName: TextInputEditText
    private lateinit var rgGender: RadioGroup
//    private lateinit var tiEdtDateOfBirth: TextInputEditText

    private lateinit var tiEdtEmail: TextInputEditText

    //    private lateinit var tiEdtUsername: TextInputEditText
    private lateinit var tiEdtPassword: TextInputEditText
    private lateinit var tiEdtConfirmPassword: TextInputEditText

    private lateinit var btnCreateAccount: Button
    private lateinit var btnClear: Button

    private lateinit var userController: UserController

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private lateinit var mStorageRef: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_signup, container, false)

        initViews()
        addActionListeners()

        return v
    }

    private fun addActionListeners() {
        tvLogin.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        btnAddProfilePic.setOnClickListener {
            setUserPhoto()
        }
        btnCreateAccount.setOnClickListener {
            createAccountAndLogin()
        }
        tiEdtFirstName.addTextChangedListener {
            val tv = v.findViewById<TextView>(R.id.tvInvalidFirstName)
            if (tv.isVisible) {
                tv.visibility = View.GONE
            }
        }
        tiEdtMiddleName.addTextChangedListener {
            val tv = v.findViewById<TextView>(R.id.tvInvalidMidName)
            if (tv.isVisible) {
                tv.visibility = View.GONE
            }
        }
        tiEdtLastName.addTextChangedListener {
            val tv = v.findViewById<TextView>(R.id.tvInvalidLastName)
            if (tv.isVisible) {
                tv.visibility = View.GONE
            }
        }
        tiEdtPassword.addTextChangedListener {
            val tv = v.findViewById<TextView>(R.id.tvPasswordShouldContain)
            if (tv.isVisible) {
                tv.visibility = View.GONE
            }
        }
        tiEdtConfirmPassword.addTextChangedListener {
            val tv = v.findViewById<TextView>(R.id.tvPasswordDoNotMatch)
            if (tv.isVisible) {
                tv.visibility = View.GONE
            }
        }
        tiEdtEmail.addTextChangedListener {
            val tv = v.findViewById<TextView>(R.id.tvInvalidEmail)
            if (tv.isVisible)
                tv.visibility = View.GONE
        }
    }

    private fun initViews() {
        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()
        mStorageRef = FirebaseStorage.getInstance().getReference()

        userController = ViewModelProvider(this)[UserController::class.java]

        tvLogin = v.findViewById(R.id.tvLogin_createAcc)
        sIvProfilePhoto = v.findViewById(R.id.sIvProfilePhoto_createAcc)
        btnAddProfilePic = v.findViewById(R.id.btnAddProfilePic_createAcc)
        tiEdtFirstName = v.findViewById(R.id.tiEdtFirstName_createAcc)
        tiEdtMiddleName = v.findViewById(R.id.tiEdtMiddleName_createAcc)
        tiEdtLastName = v.findViewById(R.id.tiEdtLastName_createAcc)
        rgGender = v.findViewById(R.id.rgGender_createAcc)
        tiEdtEmail = v.findViewById(R.id.tiEdtEmail_createAcc)

//        tiEdtUsername = v.findViewById(R.id.tiEdtEmail)
        tiEdtPassword = v.findViewById(R.id.tiEdtPassword_createAcc)
        tiEdtConfirmPassword = v.findViewById(R.id.tiEdtConfirmPassword_createAcc)


        btnCreateAccount = v.findViewById(R.id.btnCreateAccount)
        btnClear = v.findViewById(R.id.btnCancel_createAcc)
    }

    private fun setUserPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, CONST.IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == CONST.IMAGE_PICK_CODE) {
            val imageUri = data?.data
            if (imageUri != null) {
                uriProfilePhoto = imageUri
                Glide.with(requireContext())
                    .load(uriProfilePhoto)
                    .placeholder(R.drawable.kamake)
                    .centerCrop()
                    .into(sIvProfilePhoto)
            }
        }
    }

    private fun authUser(email: String, password: String) {
        lifecycleScope.launch {
            val userExists = userController.checkUser(email, password)
        }
    }

    private fun login(uid: String) {
        val editor =
            requireContext().getSharedPreferences(CONST.SHARED_PREF_db, Context.MODE_PRIVATE).edit()
        editor.putString(CONST.SHARED_PREF_USER_ID, uid)
        editor.putBoolean(CONST.SHARED_PREF_IS_USER_SIGNED_IN, true)
        editor.apply()

        val intent = Intent(requireContext(), UploadToCloudActivity::class.java)
        requireActivity().finish()
        startActivity(intent)
    }

    private fun addUserToDB(uid: String, firstName: String, middleName: String, lastName: String, email: String, password: String, gender: String) {
        if (uriProfilePhoto != null) {
            val fileExt = getFileExtension(requireContext(), uriProfilePhoto!!)

            var photoURI: String?
            var photoName: String? = "$uid.$fileExt"

            Log.d(TAG, "addUserToDB: $photoName")

            val storageRef = mStorageRef.child(CONST.fDB_DIR_IMG).child(CONST.fDB_DIR_IMG_PROF)
                .child(photoName!!)
            storageRef.putFile(uriProfilePhoto!!).addOnCompleteListener { task ->
                storageRef.downloadUrl.addOnCompleteListener { uri ->
                    photoURI = uri.result.toString()

                    val user = TblUser(uid = uid, firstName = firstName, middleName = middleName, lastName = lastName, email = email, password = password, gender = gender, photo = photoURI, registeredDate = System.currentTimeMillis())
                    addUserLocally(user)
                    addUserRemotely(user)
                }
            }
        }
    }

    private fun addUserRemotely(user: TblUser) {
        mDBRef.child(CONST.fDB_DIR_USER)
            .child(user.uid!!)
            .setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "addUserToDB: Registered")
                } else {
                    Log.d(TAG, "addUserToDB: Error Registering: ${task.exception}")
                }
            }
    }

    private fun addUserLocally(user: TblUser) {
        CoroutineScope(Dispatchers.IO).launch {
            userController.insertUser(user)
        }
    }

    private fun createAccountAndLogin() {
        if (validateInput()) {
            val firstName = tiEdtFirstName.text.toString().trim()
            val middleName = tiEdtMiddleName.text.toString().trim()
            val lastName = tiEdtLastName.text.toString().trim()
            val email = tiEdtEmail.text.toString().trim()
            val password = tiEdtPassword.text.toString().trim()
            val gender = getGender()

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        addUserToDB(
                            task.result.user!!.uid,
                            firstName,
                            middleName,
                            lastName,
                            email,
                            password,
                            gender
                        )
                        login(task.result.user!!.uid)
                        Log.d(TAG, "createAccount: ${task.exception}")
                    } else {
                        Log.d(TAG, "createAccount: ${task.exception}")
                        Toast.makeText(requireContext(), "Failed...", Toast.LENGTH_SHORT).show()
                    }
                }

//            authUser(email, password)
        }
    }

    fun getFileExtension(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }


    private fun getBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
    }

    private fun validateInput(): Boolean {
        val name = tiEdtFirstName.text.toString().trim()
        val middleName = tiEdtMiddleName.text.toString().trim()
        val lastName = tiEdtLastName.text.toString().trim()

        val email = tiEdtEmail.text.toString().trim()
        val password = tiEdtPassword.text.toString().trim()
        val passwordConfirm = tiEdtConfirmPassword.text.toString().trim()

        val bName = validateName(name)
//        val bMiddleName = validateName(middleName)
        val bLastName = validateName(lastName)
        val bPassword = if (password == passwordConfirm) validatePassword(password) else false
        val bPasswordConfirm = validatePassword(passwordConfirm)
        val bEmail = validateEmail(email)

        if (!bName)
            v.findViewById<TextView>(R.id.tvInvalidFirstName).visibility = View.VISIBLE
//        if (!bMiddleName)
//            v.findViewById<TextView>(R.id.tvInvalidMidName).visibility = View.VISIBLE
        if (!bLastName)
            v.findViewById<TextView>(R.id.tvInvalidLastName).visibility = View.VISIBLE
        if (!bPassword)
            v.findViewById<TextView>(R.id.tvPasswordShouldContain).visibility = View.VISIBLE
        if (!bPasswordConfirm)
            v.findViewById<TextView>(R.id.tvPasswordDoNotMatch).visibility = View.VISIBLE
        if (!bEmail)
            v.findViewById<TextView>(R.id.tvInvalidEmail).visibility = View.VISIBLE

        return bName && bLastName && bPassword && bPasswordConfirm && bEmail
    }


    private fun validateName(name: String): Boolean {
        return name.length >= 3
    }

    private fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        return emailRegex.matches(email)
    }

    private fun validatePassword(password: String): Boolean {
        var bP = false
        if (password.length > 6) {
//            val rDigits = Regex("""\d""")
//            val rAlphUC = Regex("""[A-Z]""")
//            val rAlphLC = Regex("""[a-z]""")
//            val rSymbols = Regex("""[!@#${'$'}%^&*()_+{}\[\];:'"\\|/?,.<>=\-`~]""")
//
//            bP = rDigits.matches(password) && rAlphUC.matches(password) && rAlphLC.matches(password) && rSymbols.matches(password)
            bP = true
        }
        return bP
    }

    private fun getGender(): String {
        return when (rgGender.checkedRadioButtonId) {
            R.id.rbGenderF_createAcc -> {
                UserGender.FEMALE.value
            }

            R.id.rbGenderM_createAcc -> {
                UserGender.MALE.value
            }

            else -> {
                UserGender.RATHER_NOT_SAY.value
            }
        }
    }

}