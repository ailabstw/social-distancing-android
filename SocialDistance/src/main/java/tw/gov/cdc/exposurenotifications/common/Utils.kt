package tw.gov.cdc.exposurenotifications.common

import android.app.AlertDialog
import android.content.Context
import androidx.annotation.StringRes
import tw.gov.cdc.exposurenotifications.R
import java.text.DateFormat
import java.util.*

object Utils {

    fun showHintDialog(context: Context, @StringRes stringRes: Int) {
        AlertDialog.Builder(context)
            .setMessage(context.getString(stringRes))
            .setPositiveButton(R.string.confirm) { _, _ -> }
            .setCancelable(false)
            .create()
            .show()
    }

    fun Calendar.toBeginOfTheDay(): Calendar {
        return this.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun Calendar.toEndOfTheDay(): Calendar {
        return this.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }

    fun Calendar.toSpecificTime(hour: Int = 0,
                                minute: Int = 0,
                                second: Int = 0,
                                millisecond: Int = 0): Calendar {
        return this.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, millisecond)
        }
    }

    fun Calendar.daysSinceEpoch(): Int {
        return (timeInMillis / 1000 / 60 / 60 / 24).toInt()
    }

    fun Calendar.setDaysSinceEpoch(daysSinceEpoch: Int): Calendar {
        return this.apply {
            timeInMillis = daysSinceEpoch.toLong() * 24 * 60 * 60 * 1000
        }
    }

    private val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    fun getDateString(date: Date): String {
        return dateFormat.format(date)
    }
}