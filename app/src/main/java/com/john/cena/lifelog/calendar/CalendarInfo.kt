package com.john.cena.lifelog.calendar

import android.provider.CalendarContract
import java.io.Serializable

class CalendarInfo : Serializable {

    var id: Long = 0
    var calendarDisplayName: String = ""
    var calendarColor: String = ""
    var accountName: String = ""
    var accountType: String = ""
    var isEnabled: Boolean = false

    override fun toString(): String {
        return this.calendarDisplayName
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is CalendarInfo) {
            this.id == obj.id
        } else false

    }

    companion object {
        private const val serialVersionUID = 8137237806877422485L

        val EVENT_PROJECTION = arrayOf(CalendarContract.Calendars._ID, // 0
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, // 1
                CalendarContract.Calendars.CALENDAR_COLOR, // 2
                CalendarContract.Calendars.ACCOUNT_NAME, // 3
                CalendarContract.Calendars.ACCOUNT_TYPE                 // 4
        )

        val PROJECTION_ID_INDEX = 0
        val PROJECTION_DISPLAY_NAME_INDEX = 1
        val PROJECTION_CALENDAR_COLOR = 2
        val PROJECTION_ACCOUNT_NAME = 3
        val PROJECTION_ACCOUNT_TYPE = 4
    }
}