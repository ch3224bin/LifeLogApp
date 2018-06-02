package com.john.cena.lifelog.write

import android.content.Context
import com.john.cena.lifelog.WriteFragment
import java.util.*

/**
 * 현재 진행중인 이벤트 생성, 삭제.
 */
class CurrentEventStore constructor(private val context: Context){
    val eventCalendarPref by lazy {
        context.getSharedPreferences(WriteFragment.EVENT_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun save(writeVo: WriteVo) {
        eventCalendarPref.edit()
                .putString(WriteFragment.KEY_STATE, WriteFragment.STATE_PROGRESS)
                .putLong(WriteFragment.KEY_CATEGORY, writeVo.categoryId)
                .putString(WriteFragment.KEY_TITLE, writeVo.title)
                .putString(WriteFragment.KEY_CONTENT, writeVo.content)
                .putLong(WriteFragment.KEY_START_TIME, writeVo.startTime)
                .apply()
    }

    fun clear() {
        eventCalendarPref.edit()
                .putString(WriteFragment.KEY_STATE, WriteFragment.STATE_FINISH)
                .remove(WriteFragment.KEY_CATEGORY)
                .remove(WriteFragment.KEY_TITLE)
                .remove(WriteFragment.KEY_CONTENT)
                .apply()
    }

}