package com.john.cena.lifelog.calendar

import android.Manifest
import android.annotation.SuppressLint
import android.content.AsyncQueryHandler
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.support.v4.app.ActivityCompat
import com.john.cena.lifelog.write.WriteVo
import java.text.SimpleDateFormat
import java.util.*

class CalendarManager(private val context: Context) {
    private val SHARED_PREF_FILE_NAME = "my_calendar"
    private val ENABLED_CALENDAR_FILE_NAME = "enabled_calendar"
    private val PREF_CALENDAR_ENABLED_SETTING_NAME = "settings_enabled"
    private val settingsEnabledPref: SharedPreferences
    private val enabledCalendarPref: SharedPreferences

    val enabledCalendarList: List<CalendarInfo>
        get() {
            val calendarInfoList = ArrayList<CalendarInfo>()
            val map = enabledCalendarPref.all
            for (key in map.keys) {
                val info = CalendarInfo()
                info.id = java.lang.Long.valueOf(key)
                info.calendarDisplayName = map[key] as String
                calendarInfoList.add(info)
            }

            return calendarInfoList
        }

    init {
        this.settingsEnabledPref = context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
        this.enabledCalendarPref = context.getSharedPreferences(ENABLED_CALENDAR_FILE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 캘린더 가져오기.
     */
    fun getCalendars(adapter: CalendarsAdapter) {
        val mQueryHandler = CalendarsQueryHandler(context, adapter)
        mQueryHandler.startQuery(0, null,
                CalendarContract.Calendars.CONTENT_URI,
                CalendarInfo.EVENT_PROJECTION, null, null, null)
    }

    /**
     * 이벤트 가져오기
     */
    fun getEvents(adapter: EventAdapter, enabledCalendarList: List<CalendarInfo>,
                  eventInfo: EventInfo, limit: Int, offset: Int, queryCallBack: (Int) -> Unit) {
        val mQueryHandler = EventsQueryHandler(context, adapter, queryCallBack)

        if (enabledCalendarList.isEmpty()) {
            return
        }

        val selection = StringBuilder()
        val selectionArgList = ArrayList<String>()
        selection.append(" (")
        var i = 0
        val n = enabledCalendarList.size
        while (i < n) {
            selection.append(CalendarContract.Events.CALENDAR_ID + " = ?")
            selectionArgList.add(enabledCalendarList[i].id.toString())
            if (i < n - 1) {
                selection.append(" OR ")
            }
            i++
        }
        selection.append(") ")

        if (eventInfo.id > 0) {
            selection.append(" AND " + CalendarContract.Events._ID + " = ? ")
            selectionArgList.add(eventInfo.id.toString())
        }

        mQueryHandler.startQuery(0, null,
                CalendarContract.Events.CONTENT_URI,
                EventInfo.EVENT_PROJECTION,
                selection.toString(),
                selectionArgList.toTypedArray(),
                CalendarContract.Events.DTSTART + " DESC LIMIT " + limit + " OFFSET " + offset)
    }

    /**
     * 캘린더 활성화
     */
    fun setCalendarEnabled(calendarInfo: CalendarInfo) {
        settingsEnabledPref.edit()
                .putBoolean(PREF_CALENDAR_ENABLED_SETTING_NAME + calendarInfo.id, calendarInfo.isEnabled)
                .apply()

        // 캘린더 활성화 된 것만 따로 모아서, 이벤트 입력시 스피너에서 사용
        if (calendarInfo.isEnabled) {
            enabledCalendarPref.edit()
                    .putString(calendarInfo.id.toString(), calendarInfo.calendarDisplayName)
                    .apply()
        } else {
            enabledCalendarPref.edit()
                    .remove(calendarInfo.id.toString())
                    .apply()
        }
    }

    /**
     * 이벤트 삽입
     */
    fun insertEvent(vo: WriteVo): Uri? {
        val cr = context.contentResolver
        val values = ContentValues()
        values.put(CalendarContract.Events.DTSTART, vo.startTime)
        values.put(CalendarContract.Events.DTEND, vo.endTime)
        values.put(CalendarContract.Events.TITLE, vo.title)
        values.put(CalendarContract.Events.DESCRIPTION, vo.content)
        values.put(CalendarContract.Events.CALENDAR_ID, vo.categoryId)
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            null
        } else cr.insert(CalendarContract.Events.CONTENT_URI, values)

        /* 동기화 어댑터
            Log.d("테스트", "타임존 " + TimeZone.getDefault().getID());
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            Uri uri2 = asSyncAdapter(CalendarContract.Events.CONTENT_URI, accountName, accountType);
            ops.add(ContentProviderOperation.newInsert(uri2)
                    .withValue(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                    .withValue(CalendarContract.Calendars.ACCOUNT_TYPE, accountType)
                    .withValue(CalendarContract.Events.DTSTART, startMillis)
                    .withValue(CalendarContract.Events.DTEND, endMillis)
                    .withValue(CalendarContract.Events.TITLE, title)
                    .withValue(CalendarContract.Events.DESCRIPTION, description)
                    .withValue(CalendarContract.Events.CALENDAR_ID, calID)
                    .withValue(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID())
                    .build());
            try {
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                Log.e("테스트", e.getMessage());
            }*/
    }

    /*    private Uri asSyncAdapter(Uri uri, String account, String accountType) {
        return uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType).build();
    }*/

    private fun cursorToCalendarInfo(cursor: Cursor): CalendarInfo {
        val calID = cursor.getLong(CalendarInfo.PROJECTION_ID_INDEX)
        val displayName = cursor.getString(CalendarInfo.PROJECTION_DISPLAY_NAME_INDEX)
        val color = cursor.getString(CalendarInfo.PROJECTION_CALENDAR_COLOR)
        val accountName = cursor.getString(CalendarInfo.PROJECTION_ACCOUNT_NAME)
        val accountType = cursor.getString(CalendarInfo.PROJECTION_ACCOUNT_TYPE)

        val info = CalendarInfo()
        info.id = calID
        info.calendarDisplayName = displayName
        info.calendarColor = color
        info.accountName = accountName
        info.accountType = accountType

        return info
    }

    /**
     * 캘린더 쿼리 핸들러
     */
    private inner class CalendarsQueryHandler(context: Context, private val mAdapter: CalendarsAdapter) : AsyncQueryHandler(context.contentResolver) {

        override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?) {
            if (cursor == null) {
                return
            }
            while (cursor.moveToNext()) {
                val info = cursorToCalendarInfo(cursor)
                // 로컬에서 해당 캘린더가 활성화 되어 있는지 확인
                info.isEnabled = settingsEnabledPref.getBoolean(PREF_CALENDAR_ENABLED_SETTING_NAME + info.id, false)
                mAdapter.addItem(info)
            }
            mAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 이벤트 쿼리 핸들러
     */
    private inner class EventsQueryHandler(context: Context, private val mAdapter: EventAdapter, private val queryCallBack: (Int) -> Unit) : AsyncQueryHandler(context.contentResolver) {

        /**
         * 단일 캘린더 가져오기
         */
        @SuppressLint("MissingPermission")
        private fun getCalendar(calID: Long): CalendarInfo? {

            val selection = "(" + CalendarContract.Calendars._ID + " = ?)"
            val selectionArgs = arrayOf(calID.toString())

            val cursor = context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, CalendarInfo.EVENT_PROJECTION, selection, selectionArgs, null)
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    return cursorToCalendarInfo(cursor)
                }
            }

            return null
        }

        override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?) {
            if (cursor == null) {
                queryCallBack(0)
                return
            }
            while (cursor.moveToNext()) {
                val id = cursor.getLong(EventInfo.PROJECTION_ID_INDEX)
                val calID = cursor.getLong(EventInfo.PROJECTION_CALENDAR_ID)
                val title = cursor.getString(EventInfo.PROJECTION_TITLE)
                val description = cursor.getString(EventInfo.PROJECTION_DESCRIPTION)
                val dtstart = cursor.getLong(EventInfo.PROJECTION_DTSTART)
                val dtend = cursor.getLong(EventInfo.PROJECTION_DTEND)

                var category = ""
                val calendarInfo = getCalendar(calID)
                if (calendarInfo != null) {
                    category = calendarInfo.calendarDisplayName
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val startTime = sdf.format(Date(dtstart))
                val endTime = sdf.format(Date(dtend))

                val info = EventInfo(id, calID, category, title, description, startTime, endTime, "")
                info.dtstart = dtstart
                info.dtend = dtend
                mAdapter.addItem(info)
            }
            mAdapter.notifyDataSetChanged()
            queryCallBack(cursor.count)
        }
    }
}
