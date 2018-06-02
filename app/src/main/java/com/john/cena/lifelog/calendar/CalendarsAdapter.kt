package com.john.cena.lifelog.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import com.john.cena.lifelog.R
import java.util.ArrayList

class CalendarsAdapter(context: Context, private val layout: Int) : BaseAdapter() {
    private val data = ArrayList<CalendarInfo>()
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var onCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null

    fun setOnCheckedChangeListener(onCheckedChangeListener: CompoundButton.OnCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener
    }

    fun addItem(calendarInfo: CalendarInfo) {
        data.add(calendarInfo)
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return data[position].id
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false)
        }
        val calendarInfo = data[position]

        // 캘린더 이름
        val name = convertView!!.findViewById<TextView>(R.id.textView)
        name.text = calendarInfo.calendarDisplayName

        // 캘린더 사용여부
        val switch1 = convertView.findViewById<Switch>(R.id.switch1)
        switch1.isChecked = calendarInfo.isEnabled
        switch1.tag = position // switch의 position 값을 tag에 담는다.
        if (onCheckedChangeListener != null) {
            switch1.setOnCheckedChangeListener(onCheckedChangeListener)
        }

        return convertView
    }
}