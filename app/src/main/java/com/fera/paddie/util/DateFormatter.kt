package com.fera.paddie.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateFormatter {
    companion object {
        fun formatDate(date: Date?): String? {
            val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            return date?.let { formatter.format(it) }
        }

        fun formatDateAndTime(date: Date?): String? {
            val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            return date?.let { formatter.format(it) }
        }
    }
}