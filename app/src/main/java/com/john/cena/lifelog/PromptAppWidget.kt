package com.john.cena.lifelog

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import android.widget.Toast
import com.john.cena.lifelog.calendar.CalendarInfo
import com.john.cena.lifelog.calendar.CalendarManager
import com.john.cena.lifelog.write.CurrentEventStore
import com.john.cena.lifelog.write.WriteVo
import io.reactivex.Observable
import java.util.*


/**
 * Implementation of App Widget functionality.
 */
class PromptAppWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val manager = AppWidgetManager.getInstance(context)
        val appWidgetIds = manager.getAppWidgetIds(ComponentName("com.john.cena.lifelog",
                "com.john.cena.lifelog.PromptAppWidget"))

        if (intent.action == ACTION_BUTTON_CLICK || intent.action == ACTION_NEXT_CLICK) {
            Observable.just(
                    when(intent.action) {
                        ACTION_BUTTON_CLICK -> buttonClick(context)
                        else -> nextButtonClick(context) // ACTION_NEXT_CLICK
                    }
            ).subscribe({
                    onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds)
            })
        }

        if (intent.action == ACTION_MAIN_APP_STOP) {
            onUpdate(context, manager, appWidgetIds)
        }
    }

    private fun buttonClick(context: Context) {
        val cm = CalendarManager(context)
        val currentEventStore = CurrentEventStore(context)
        val eventCalendarPref =  currentEventStore.eventCalendarPref
        val currentCategory = getCurrentCategory(context, eventCalendarPref, cm)

        val writeVo = WriteVo(currentCategory.id, "", "")
        val state = eventCalendarPref.getString(WriteFragment.KEY_STATE, WriteFragment.STATE_FINISH)
        if (WriteFragment.STATE_FINISH == state) { // 종료상태. -> 진행 상태로 변경
            writeVo.state = WriteFragment.STATE_PROGRESS
            writeVo.startTime = Calendar.getInstance().timeInMillis
            currentEventStore.save(writeVo)

            Toast.makeText(context, R.string.message_start, Toast.LENGTH_SHORT).show()
        } else {
            writeVo.state = WriteFragment.STATE_FINISH
            writeVo.title = eventCalendarPref.getString(WriteFragment.KEY_TITLE, "")
            writeVo.content = eventCalendarPref.getString(WriteFragment.KEY_CONTENT, "")
            writeVo.startTime = eventCalendarPref.getLong(WriteFragment.KEY_START_TIME, Calendar.getInstance().timeInMillis)
            writeVo.endTime = Calendar.getInstance().timeInMillis

            cm.insertEvent(writeVo)
            currentEventStore.clear()

            Toast.makeText(context, R.string.message_finish, Toast.LENGTH_SHORT).show()
        }
    }

    private fun nextButtonClick(context: Context) {
        val cm = CalendarManager(context)
        val eventCalendarPref = CurrentEventStore(context).eventCalendarPref
        val enabledCalendarList = cm.enabledCalendarList
        val id = eventCalendarPref.getLong(WriteFragment.KEY_CATEGORY, enabledCalendarList[0].id)

        for (i in enabledCalendarList.indices) {
            if (enabledCalendarList[i].id == id) {
                val nextId =
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
        const val ACTION_MAIN_APP_STOP = "ACTION_MAIN_APP_STOP"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.prompt_app_widget)

            // 이벤트
            views.setOnClickPendingIntent(R.id.button,
                    getPendingIntent(context, appWidgetId, ACTION_BUTTON_CLICK))

            views.setOnClickPendingIntent(R.id.nextButton,
                    getPendingIntent(context, appWidgetId, ACTION_NEXT_CLICK))

            val eventCalendarPref = CurrentEventStore(context).eventCalendarPref

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
            val intent = Intent(action)
            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }
    }
}

