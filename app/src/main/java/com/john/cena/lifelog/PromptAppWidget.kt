package com.john.cena.lifelog

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import android.widget.Toast
import com.john.cena.lifelog.calendar.CalendarInfo
import com.john.cena.lifelog.calendar.CalendarManager
import com.john.cena.lifelog.write.WriteVo
import io.reactivex.Observable
import java.util.*


/**
 * Implementation of App Widget functionality.
 */
class PromptAppWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when(intent.action) {
            ACTION_BUTTON_CLICK -> buttonClick(context)
            ACTION_NEXT_CLICK -> nextButtonClick(context)
        }

        if (intent.action == ACTION_BUTTON_CLICK || intent.action == ACTION_NEXT_CLICK) {
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            onUpdate(context, AppWidgetManager.getInstance(context), intArrayOf(id))
        }
    }

    private fun buttonClick(context: Context) {
        var cm = CalendarManager(context)
        val eventCalendarPref = context.getSharedPreferences(WriteFragment.EVENT_FILE_NAME, Context.MODE_PRIVATE)
        val currentCategory = getCurrentCategory(context, eventCalendarPref, cm)

        val writeVo = WriteVo(currentCategory.id, "", "")
        val state = eventCalendarPref.getString(WriteFragment.KEY_STATE, WriteFragment.STATE_FINISH)
        if (WriteFragment.STATE_FINISH == state) { // 종료상태. -> 진행 상태로 변경
            eventCalendarPref.edit()
                    .putString(WriteFragment.KEY_STATE, WriteFragment.STATE_PROGRESS)
                    .putLong(WriteFragment.KEY_CATEGORY, writeVo.categoryId)
                    .putString(WriteFragment.KEY_TITLE, writeVo.title)
                    .putString(WriteFragment.KEY_CONTENT, writeVo.content)
                    .putLong(WriteFragment.KEY_START_TIME, Calendar.getInstance().timeInMillis)
                    .apply()

            Toast.makeText(context, R.string.message_start, Toast.LENGTH_SHORT).show()
        } else {
            writeVo.startTime = eventCalendarPref.getLong(WriteFragment.KEY_START_TIME, Calendar.getInstance().timeInMillis)
            writeVo.endTime = Calendar.getInstance().timeInMillis

            Observable.just(cm.insertEvent(writeVo))
                    .subscribe({
                        eventCalendarPref.edit()
                                .putString(WriteFragment.KEY_STATE, WriteFragment.STATE_FINISH)
                                .remove(WriteFragment.KEY_CATEGORY)
                                .remove(WriteFragment.KEY_TITLE)
                                .remove(WriteFragment.KEY_CONTENT)
                                .apply()

                        Toast.makeText(context, R.string.message_finish, Toast.LENGTH_SHORT).show()
                    })
        }
    }

    private fun nextButtonClick(context: Context) {
        var cm = CalendarManager(context)
        val eventCalendarPref = context.getSharedPreferences(WriteFragment.EVENT_FILE_NAME, Context.MODE_PRIVATE)
        val enabledCalendarList = cm.enabledCalendarList
        val id = eventCalendarPref.getLong(WriteFragment.KEY_CATEGORY, enabledCalendarList[0]?.id)

        for (i in enabledCalendarList.indices) {
            if (enabledCalendarList[i].id == id) {
                var nextId =
                        if (i == enabledCalendarList.size - 1) {
                            enabledCalendarList[0].id
                        } else {
                            enabledCalendarList[i + 1].id
                        }

                eventCalendarPref.edit()
                        .putLong(WriteFragment.KEY_CATEGORY, nextId)
                        .apply()
                break
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {

        const val ACTION_BUTTON_CLICK = "ACTION_BUTTON_CLICK"
        const val ACTION_NEXT_CLICK = "ACTION_NEXT_CLICK"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.prompt_app_widget)

            // 이벤트
            views.setOnClickPendingIntent(R.id.button,
                    getPendingIntent(context, appWidgetId, ACTION_BUTTON_CLICK))

            views.setOnClickPendingIntent(R.id.nextButton,
                    getPendingIntent(context, appWidgetId, ACTION_NEXT_CLICK))

            val eventCalendarPref = context.getSharedPreferences(WriteFragment.EVENT_FILE_NAME, Context.MODE_PRIVATE)

            // 시작-종료 버튼 이미지
            val stat = eventCalendarPref.getString(WriteFragment.KEY_STATE, WriteFragment.STATE_FINISH)
            val buttonImage =
                    if (stat == WriteFragment.STATE_PROGRESS)
                        android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
            views.setImageViewResource(R.id.button, buttonImage)

            // 카테고리 다음 버튼 이미지
            views.setImageViewResource(R.id.nextButton, android.R.drawable.ic_media_next)

            // 카테고리
            val currentCategory = getCurrentCategory(context, eventCalendarPref)
            views.setTextViewText(R.id.textView, currentCategory.calendarDisplayName)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getCurrentCategory(context: Context, eventCalendarPref: SharedPreferences, cm: CalendarManager = CalendarManager(context)) : CalendarInfo {
            val enabledCalendarList = cm.enabledCalendarList
            val id = eventCalendarPref.getLong(WriteFragment.KEY_CATEGORY, 0)
            var calenderInfo = enabledCalendarList[0]
            for (info in enabledCalendarList) {
                if (info.id == id) {
                    calenderInfo = info
                    break
                }
            }

            return calenderInfo
        }

        private fun getPendingIntent(context: Context, appWidgetId: Int, action: String): PendingIntent {
            var intent = Intent(action)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }
    }
}

