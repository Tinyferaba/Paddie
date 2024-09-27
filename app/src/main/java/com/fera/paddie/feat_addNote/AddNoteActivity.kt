package com.fera.paddie.feat_addNote

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.fera.paddie.R
import com.fera.paddie.common.controller.NoteControllers
import com.fera.paddie.common.model.TblNote
import com.fera.paddie.util.CONST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class AddNoteActivity : AppCompatActivity() {
    private val TAG = "AddNoteActivity"

    //######### STATE & VALUES #########//
    private var changesMade = false
    private var favourite = false
    private lateinit var tblNote: TblNote
    private var currentTextGravity: TextGravity = TextGravity.GRAVITY

    private lateinit var ivBack: ImageView      //Image View
    private lateinit var ivSave: ImageView
    private lateinit var ivEdit: ImageView
    private lateinit var edtTitle: EditText     //Edit Text
    private lateinit var edtDesc: EditText

    private lateinit var tvDisplayFontSize: TextView        //  Font Properties
    private lateinit var includeProperties: View
    private lateinit var ivIncreaseFontSize: ImageView
    private lateinit var ivDecreaseFontSize: ImageView
    private lateinit var ivBold: ImageView
    private lateinit var ivItalicize: ImageView
    private lateinit var ivRestoreFontProps: ImageView
    private lateinit var ivShowHideFontProperties: ImageView

    //Bottom Nav
    private lateinit var ivAlignLeft: ImageView
    private lateinit var ivAlignCenter: ImageView
    private lateinit var ivAlignRight: ImageView
    private lateinit var ivFavourite: ImageView

    private lateinit var noteControllers: NoteControllers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_note)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        addActionListeners()

        setTextProperties()
        loadNote()

        setStatusBarColor()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addActionListeners() {
        ivBack.setOnClickListener {
            if (changesMade) {
                saveNote()
                changesMade = false
                toggleEditability(false)
            }
            onBackPressed()
        }
        ivSave.setOnClickListener {
            if (tblNote.pkNoteId == null) {
                saveNote()
            } else {
                updateNote()
            }
            changesMade = false
            toggleEditability(false)
        }
        ivEdit.setOnClickListener {
            toggleEditability(true)
        }

        includeProperties.setOnClickListener {
            //Nothing set yet...
        }
        var isIncreasing = false
        ivIncreaseFontSize.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ivIncreaseFontSize.setBackgroundResource(R.drawable.bg_touch_round)

                    isIncreasing = true
                    CoroutineScope(Dispatchers.Main).launch {
                        while (isIncreasing) {
                            increaseFontSize()
                            delay(200)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    ivIncreaseFontSize.background = null
                    isIncreasing = false
                    true
                }

                else -> false
            }
        }
        ivDecreaseFontSize.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ivDecreaseFontSize.setBackgroundResource(R.drawable.bg_touch_round)
                    isIncreasing = true
                    CoroutineScope(Dispatchers.Main).launch {
                        while (isIncreasing) {
                            decreaseFontSize()
                            delay(200)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    ivDecreaseFontSize.background = null
                    isIncreasing = false
                    true
                }

                else -> false
            }
        }
        ivIncreaseFontSize.setOnClickListener {
            increaseFontSize()
        }
        ivDecreaseFontSize.setOnClickListener {
            decreaseFontSize()
        }
        ivBold.setOnClickListener {
            val isItalic = edtDesc.typeface.isItalic
            val isBold = !edtDesc.typeface.isBold

            toggleBoldAndItalic(isBold, isItalic)
        }
        ivItalicize.setOnClickListener {
            val isBold = edtDesc.typeface.isBold
            val isItalic = !edtDesc.typeface.isItalic

            toggleBoldAndItalic(isBold, isItalic)
        }

        ivRestoreFontProps.setOnClickListener {
            val fontProperties = _Font.defaultFontProperties()

            val boldColor = ContextCompat.getColor(this, R.color.gray)
            ivBold.setColorFilter(boldColor, PorterDuff.Mode.SRC_IN)

            val italicColor = ContextCompat.getColor(this, R.color.gray)
            ivItalicize.setColorFilter(italicColor, PorterDuff.Mode.SRC_IN)

            edtDesc.setTypeface(fontProperties.typeface)
            edtDesc.textSize = fontProperties.fontSize
        }
        ivShowHideFontProperties.setOnClickListener {
            if (includeProperties.isVisible) {
                hideFontPropertiesPanel()
            } else {
                showFontPropertiesPanel()
            }
        }

        ivAlignLeft.setOnClickListener {
            changeGravity(TextGravity.LEFT)
        }
        ivAlignCenter.setOnClickListener {
            changeGravity(TextGravity.CENTER)
        }
        ivAlignRight.setOnClickListener {
            changeGravity(TextGravity.RIGHT)
        }

        ivFavourite.setOnClickListener {
            favourite = !favourite

            if (favourite) {
                ivFavourite.setImageResource(R.drawable.ic_favourite)
            } else {
                ivFavourite.setImageResource(R.drawable.ic_unfavourite)
            }

            tblNote.favourite = favourite
            if (tblNote.pkNoteId != null)
                updateFavourite(tblNote.pkNoteId!!, favourite)
        }

        //######### LISTEN TO CHANGES MADE #########//
        edtTitle.addTextChangedListener { changesMade() }
        edtDesc.addTextChangedListener { changesMade() }
    }

    private fun initViews() {
        tblNote = TblNote()
        noteControllers = NoteControllers(application)

        //######### VIEWS #########//
        ivBack = findViewById(R.id.ivBackAddNoteTodo_addNote)
        ivSave = findViewById(R.id.ivSaveNote_addNote)
        ivEdit = findViewById(R.id.ivEditNote_addNote)
        edtTitle = findViewById(R.id.edtTitleNote_addNote)
        edtDesc = findViewById(R.id.edtDescNote_addNote)

        tvDisplayFontSize = findViewById(R.id.tvFontSizeDisplay_addNote)                              // Font Properties
        includeProperties = findViewById(R.id.include_fontProperties_addNote)
        ivIncreaseFontSize = findViewById(R.id.ivIncreaseFontSize_addNote)
        ivDecreaseFontSize = findViewById(R.id.ivDecreaseFontSize_addNote)
        ivBold = findViewById(R.id.ivBoldText)
        ivItalicize = findViewById(R.id.ivItalicizedText)
        ivRestoreFontProps = findViewById(R.id.ivRestoreFontProperties)
        ivShowHideFontProperties = findViewById(R.id.ivShowHideFontProp_addNote)


        ivAlignLeft = findViewById(R.id.ivAlignLeft)        //Bottom Nav
        ivAlignCenter = findViewById(R.id.ivAlignCenter)
        ivAlignRight = findViewById(R.id.ivAlignRight)
        ivFavourite = findViewById(R.id.ivFavourite_addNote)
    }

    private fun setTextProperties() {
        val sharedPreferences = getSharedPreferences(CONST.SHARED_PREF_FONT, MODE_PRIVATE)
        val fontProperties = _Font.defaultFontProperties()

        edtDesc.textSize = sharedPreferences.getFloat(CONST.FONT_SIZE, fontProperties.fontSize)
        val isBold = sharedPreferences.getBoolean(CONST.IS_BOLD, fontProperties.typeface.isBold)
        val isItalic = sharedPreferences.getBoolean(CONST.IS_ITALIC, fontProperties.typeface.isItalic)

        toggleBoldAndItalic(isBold, isItalic)


        val textGravity = sharedPreferences.getString(TextGravity.GRAVITY.name, TextGravity.LEFT.name)

        when (textGravity) {
            TextGravity.LEFT.name -> {
                changeGravity(TextGravity.LEFT)
            }

            TextGravity.CENTER.name -> {
                changeGravity(TextGravity.CENTER)
            }

            TextGravity.RIGHT.name -> {
                changeGravity(TextGravity.RIGHT)
            }
            else -> {
                changeGravity(TextGravity.LEFT)
            }
        }
    }


    private fun changeGravity(newTextGravity: TextGravity) {
        val selectedColor = ContextCompat.getColor(this, R.color.white)
        val unselectedColor = ContextCompat.getColor(this, R.color.gray)

        if (newTextGravity != currentTextGravity) {
            //Unselect alignment
            when (currentTextGravity) {
                TextGravity.LEFT -> {
                    ivAlignLeft.setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN)
                }

                TextGravity.CENTER -> {
                    ivAlignCenter.setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN)
                }

                TextGravity.RIGHT -> {
                    ivAlignRight.setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN)
                }

                else -> {  }
            }

            //Select alignment
            when (newTextGravity) {
                TextGravity.LEFT -> {
                    edtDesc.gravity = android.view.Gravity.START
                    ivAlignLeft.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)
                }

                TextGravity.CENTER -> {
                    edtDesc.gravity = android.view.Gravity.CENTER_HORIZONTAL
                    ivAlignCenter.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)
                }

                TextGravity.RIGHT -> {
                    edtDesc.gravity = android.view.Gravity.END
                    ivAlignRight.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)
                }

                else -> {  }
            }

            currentTextGravity = newTextGravity
        }
    }


    private fun loadNote() {
        val tmpNote = intent.getParcelableExtra<TblNote>(CONST.KEY_TBL_NOTE)

        if (tmpNote != null)
            setData(tmpNote)
    }

    private fun setData(tmpNote: TblNote) {
        tblNote = tmpNote

        edtTitle.setText(tmpNote.title)
        edtDesc.setText(tmpNote.description)

        if (tmpNote.favourite) {
            favourite = true
            ivFavourite.setImageResource(R.drawable.ic_favourite)
        } else {
            favourite = false
            ivFavourite.setImageResource(R.drawable.ic_unfavourite)
        }

        toggleEditability(true)
    }


    private fun toggleEditability(editMode: Boolean) {
        if (editMode) {
            ivSave.visibility = View.VISIBLE
            ivEdit.visibility = View.GONE
            edtTitle.isEnabled = true
            edtDesc.isEnabled = true
        } else {
            ivSave.visibility = View.GONE
            ivEdit.visibility = View.VISIBLE
            edtTitle.isEnabled = false
            edtDesc.isEnabled = false
        }
    }

    private fun saveNote() {
        if (validateNote()) {
            CoroutineScope(Dispatchers.IO).launch {
                tblNote.title = edtTitle.text.toString()
                tblNote.description = edtDesc.text.toString()
                tblNote.favourite = favourite
                tblNote.dateCreated = Date().time
                tblNote.dateModified = Date().time

                val pkNoteId = noteControllers.insertNote(tblNote)
                tblNote.pkNoteId = pkNoteId

                withContext(Dispatchers.Main) {
                    Log.d(TAG, "saveNote: $tblNote")
                }
            }
        }
    }

    private fun validateNote(): Boolean {
        val title = edtTitle.text.isNotEmpty()
        val desc = edtDesc.text.isNotEmpty()

        return (title || desc)
    }

    private fun changesMade() {
        changesMade = true
        ivSave.visibility = View.VISIBLE
    }

    private fun updateFavourite(id: Int, favourite: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            noteControllers.updateFavourite(id, favourite)
        }
    }

    private fun toggleDisplayFontSize() {
        /*  ######### Description #########
            Displays the Font size for 2 seconds and hide the TextView
            when the increase/decrease font size btn is pressed
        */
        CoroutineScope(Dispatchers.Main).launch {
            tvDisplayFontSize.visibility = View.VISIBLE
            delay(700)
            tvDisplayFontSize.visibility = View.GONE
        }
    }

    private fun updateNote() {
        if (validateNote()) {
            CoroutineScope(Dispatchers.IO).launch {
                tblNote.title = edtTitle.text.toString()
                tblNote.description = edtDesc.text.toString()
                tblNote.favourite = favourite
                tblNote.dateModified = Date().time

                noteControllers.updateNote(tblNote)
            }
        }
    }

    private fun saveTextProps() {
        val isItalic = edtDesc.typeface.isItalic
        val isBold = edtDesc.typeface.isBold
        val fontSizePx = edtDesc.textSize     // This is given in pixels
        val fontSizeSp = fontSizePx / resources.displayMetrics.density  //Converting to SP

        val sharedPref = getSharedPreferences(CONST.SHARED_PREF_FONT, MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(CONST.IS_BOLD, isBold)
        editor.putBoolean(CONST.IS_ITALIC, isItalic)
        editor.putFloat(CONST.FONT_SIZE, fontSizeSp)

        editor.putString(TextGravity.GRAVITY.name, currentTextGravity.name)

        editor.apply()
    }

    private fun decreaseFontSize() {
        val fontSizePx= edtDesc.textSize
        var fontSizeSp = fontSizePx / resources.displayMetrics.density
        if (fontSizeSp > 10) {
            fontSizeSp -= 1
            edtDesc.textSize = fontSizeSp
            tvDisplayFontSize.text = fontSizeSp.toString()
            toggleDisplayFontSize()
        } else {
            Toast.makeText(this, "Min reached: 10sp!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun increaseFontSize() {
        val fontSizePx = edtDesc.textSize
        var fontSizeSp = fontSizePx / resources.displayMetrics.density
        if (fontSizeSp < 25) {
            fontSizeSp += 1
            edtDesc.textSize = fontSizeSp
            tvDisplayFontSize.text = fontSizeSp.toString()
            toggleDisplayFontSize()
        } else {
            Toast.makeText(this, "Max reached: 25sp!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideFontPropertiesPanel() {
        includeProperties.visibility = View.GONE
        ivShowHideFontProperties.setImageResource(R.drawable.ic_arrow_double_left)
    }

    private fun showFontPropertiesPanel() {
        includeProperties.visibility = View.VISIBLE
        ivShowHideFontProperties.setImageResource(R.drawable.ic_arrow_double_right)
    }

    private fun toggleBoldAndItalic(bold: Boolean, italic: Boolean) {
        /*  ######### Description #########
            Toggles between bold, italic and default when bold/italic btn is pressed
        */
        if (bold) {
            if (italic) {
                edtDesc.setTypeface(edtDesc.typeface, Typeface.BOLD_ITALIC)
            } else {
                edtDesc.setTypeface(edtDesc.typeface, Typeface.BOLD)
            }
        } else if (italic) {
            edtDesc.setTypeface(edtDesc.typeface, Typeface.ITALIC)
        } else {
            edtDesc.setTypeface(Typeface.DEFAULT)
        }

        //setting colors
        if (bold) {
            val boldColor = ContextCompat.getColor(this, R.color.white)
            ivBold.setColorFilter(boldColor, PorterDuff.Mode.SRC_IN)
        } else {
            val boldColor = ContextCompat.getColor(this, R.color.gray)
            ivBold.setColorFilter(boldColor, PorterDuff.Mode.SRC_IN)
        }

        //setting colors
        if (italic) {
            val italicColor = ContextCompat.getColor(this, R.color.white)
            ivItalicize.setColorFilter(italicColor, PorterDuff.Mode.SRC_IN)
        } else {
            val italicColor = ContextCompat.getColor(this, R.color.gray)
            ivItalicize.setColorFilter(italicColor, PorterDuff.Mode.SRC_IN)
        }
    }

    override fun onPause() {
        saveTextProps()
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (changesMade)
            saveNote()
    }

    private fun setStatusBarColor() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            decorView.systemUiVisibility =
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}