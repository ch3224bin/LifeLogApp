package com.john.cena.lifelog.calendar

import android.provider.CalendarContract
import java.io.Serializable

class EventInfo : Serializable {

    var id: Long = 0
    var calId: Long = 0
    var category: String = ""
    var title: String = ""
    var content: String = ""
    var startTime: String = ""
    var endTime: String = ""
    var location: String = ""
    var dtstart: Long = 0
    var dtend: Long = 0

    constructor() {

    }

    constructor(id: Long) {
        this.id = id
    }

    constructor(id: Long, calId: Long, category: String, title: String, content: String, startTime: String, endTime: String, location: String) {
        this.id = id
        this.calId = calId
        this.category = category
        this.title = title
        this.content = content
        this.startTime = startTime
        this.endTime = endTime
        this.location = location
    }

    companion object {

        val EVENT_PROJECTION = arrayOf(CalendarContract.Events._ID, // 0
                CalendarContract.Events.CALENDAR_ID, // 1
                CalendarContract.Events.TITLE, // 2
                CalendarContract.Events.DESCRIPTION, // 3
                CalendarContract.Events.DTSTART, // 4
                CalendarContract.Events.DTEND                // 5
        )

        val PROJECTION_ID_INDEX = 0
        val PROJECTION_CALENDAR_ID = 1
        val PROJECTION_TITLE = 2
        val PROJECTION_DESCRIPTION = 3
        val PROJECTION_DTSTART = 4
        val PROJECTION_DTEND = 5
        private const val serialVersionUID = -5595140841068555701L
    }
}