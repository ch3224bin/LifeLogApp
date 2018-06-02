package com.john.cena.lifelog.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.john.cena.lifelog.R
import java.util.ArrayList

class EventAdapter(context: Context, private val layout: Int) : BaseAdapter() {
    private val data = ArrayList<EventInfo>()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    fun addItem(info: EventInfo) {
        data.add(info)
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
        val eventInfo = data[position]
        val time = convertView!!.findViewById<TextView>(R.id.time)
        val category = convertView.findViewById<TextView>(R.id.category)
        val title = convertView.findViewById<TextView>(R.id.title)
        val content = convertView.findViewById<TextView>(R.id.content)

        val minute = ((eventInfo.dtend - eventInfo.dtstart) / 1000 / 60).toInt()
        time.text = String.format("%s ~ %s, %d%s", eventInfo.startTime, eventInfo.endTime, minute, inflater.context.getString(R.string.minute))
        category.text = eventInfo.category
        title.text = eventInfo.title
        content.text = eventInfo.content

        return convertView
    }
}