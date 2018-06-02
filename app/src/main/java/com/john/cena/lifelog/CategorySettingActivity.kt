package com.john.cena.lifelog

import android.os.Bundle
import android.widget.CompoundButton
import com.john.cena.lifelog.calendar.CalendarInfo
import com.john.cena.lifelog.calendar.CalendarManager
import com.john.cena.lifelog.calendar.CalendarsAdapter
import com.john.cena.lifelog.permission.PermissionManager
import kotlinx.android.synthetic.main.activity_category_setting.*

class CategorySettingActivity : BaseActivity() {

    private val cm: CalendarManager by lazy {
        CalendarManager(this)
    }
    private val pm: PermissionManager by lazy {
        PermissionManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_setting)
    }

    public override fun onStart() {
        super.onStart()

        pm.tedPermission { getCalendars() }
    }

    private fun getCalendars() {
        val adapter = CalendarsAdapter(this, R.layout.category_list_item)
        // 캘린더 사용 여부 세팅
        val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { view: CompoundButton, isChecked: Boolean ->
            val position = view.tag as Int
            val item = adapter.getItem(position) as CalendarInfo
            item.isEnabled = isChecked
            cm.setCalendarEnabled(item)
        }
        adapter.setOnCheckedChangeListener(onCheckedChangeListener)
        category_list.adapter = adapter
        cm.getCalendars(adapter)
    }

}
