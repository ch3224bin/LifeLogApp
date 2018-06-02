package com.john.cena.lifelog

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.DataSetObserver
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.*
import com.john.cena.lifelog.calendar.*
import com.john.cena.lifelog.view.MyContextMenuInfo
import com.john.cena.lifelog.write.WriteVo
import io.reactivex.Observable
import java.util.*
import kotlinx.android.synthetic.main.fragment_write.*


class WriteFragment : Fragment() {

    private var eventQueryOffset = 0

    private val cm: CalendarManager by lazy {
        CalendarManager(context!!)
    }
    private val eventCalendarPref: SharedPreferences by lazy {
        context!!.getSharedPreferences(EVENT_FILE_NAME, Context.MODE_PRIVATE)
    }


    internal enum class EventAddPosition {
        FIRST, LAST
    }

    override fun onStart() {
        super.onStart()
        // 이벤트 목록 그리기
        eventList.removeAllViews()
        eventQueryOffset = 0
        attachEventListView(EventInfo(), EventAddPosition.LAST, 0, {})

        // 카테고리 스피너 설정
        val enabledCalendarList = cm.enabledCalendarList

        // 만약 카테고리가 없으면 카테고리 설정 화면으로 보냄.
        if (enabledCalendarList.isEmpty()) {
            Toast.makeText(context, R.string.message_category_settings_required, Toast.LENGTH_LONG).show()
            val intent = Intent(context, CategorySettingActivity::class.java)
            startActivity(intent)
        }

        setCategoryItem(enabledCalendarList)

        // 진행중이라면 저장되어 있는 값들을 화면에 뿌림.
        fillContentIfProgressState(enabledCalendarList)

        Log.d("테스트", "onStart")
    }

    /**
     * 이벤트 목록은 동적으로 생성함. 화면 스크롤을 위해서.
     */
    private fun attachEventListView(eventInfo: EventInfo, eventAddPosition: EventAddPosition, offset: Int, queryCallBack: (Int) -> Unit) {
        val adapter = EventAdapter(context!!, R.layout.event_list_item)
        adapter.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                var i = 0
                val n = adapter.count
                while (i < n) {
                    val view = adapter.getView(i, null, eventList)
                    view.tag = adapter.getItem(i) // tag에 eventInfo 저장
                    view.layoutParams = layoutParams
                    view.setBackgroundResource(R.drawable.shape_underline)
                    view.setPadding(0, 30, 0, 30)
                    if (EventAddPosition.FIRST == eventAddPosition) {
                        eventList.addView(view, 0)
                    } else {
                        eventList.addView(view)
                    }

                    // 롱클릭시 컨택스트 메뉴
                    registerForContextMenu(view)
                    i++
                }
            }
        })
        cm.getEvents(adapter, cm.enabledCalendarList, eventInfo, EVENT_QUERY_PAGE_SIZE, offset, queryCallBack)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(Menu.NONE, MENU_ID_EVENT_MODIFY, 0, R.string.modify)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val menuInfo = item!!.menuInfo as MyContextMenuInfo
        val eventInfo = menuInfo.getTag() as EventInfo
        when (item.itemId) {

            MENU_ID_EVENT_MODIFY -> {
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventInfo.id)
                val intent = Intent(Intent.ACTION_VIEW).setData(uri)
                startActivity(intent)
                return true
            }
        }

        return super.onContextItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        // 진행중이라면 현재 화면의 값 저장
        val state = eventCalendarPref.getString(KEY_STATE, STATE_FINISH)
        if (STATE_PROGRESS == state) {
            val writeVo = WriteVo((category.selectedItem as CalendarInfo).id, title.text.toString(), content.text.toString())
            saveCurrentContent(writeVo)
        }
        Log.d("테스트", "onStop")
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d("테스트", "onAttach")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("테스트", "onDetach")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_write, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 버튼 이벤트 설정
        button.setOnClickListener { handleButtonEvent() }

        // 더보기 이벤트 설정
        btnReadMore.setOnClickListener { v ->
            eventQueryOffset += EVENT_QUERY_PAGE_SIZE
            attachEventListView(EventInfo(), EventAddPosition.LAST, eventQueryOffset, {
                if (it == 0) {
                    eventQueryOffset -= EVENT_QUERY_PAGE_SIZE // TODO eventQueryOffset의 증가가 문제를 가져올 것인지..
                    Toast.makeText(context, R.string.message_no_more_list, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    /**
     * 버튼 클릭시 시작, 종료 작업 분기
     */
    private fun handleButtonEvent() {
        val writeVo = WriteVo((category.selectedItem as CalendarInfo).id, title.text.toString(), content.text.toString())
        val state = eventCalendarPref.getString(KEY_STATE, STATE_FINISH)
        if (STATE_FINISH == state) { // 종료상태. -> 진행 상태로 변경
            processStart(writeVo)
        } else {
            processFinish(writeVo)
        }
    }

    /**
     * 시작 처리
     * @param writeVo
     */
    private fun processStart(writeVo: WriteVo) {
        // 현재 입력된 값들을 file에 쓴다.
        saveCurrentContent(writeVo)

        button.setText(R.string.finish)
    }

    private fun saveCurrentContent(writeVo: WriteVo) {
        eventCalendarPref.edit()
                .putString(KEY_STATE, STATE_PROGRESS)
                .putLong(KEY_CATEGORY, writeVo.categoryId)
                .putString(KEY_TITLE, writeVo.title)
                .putString(KEY_CONTENT, writeVo.content)
                .putLong(KEY_START_TIME, Calendar.getInstance().timeInMillis)
                .apply()
    }

    /**
     * 종료 처리
     * @param writeVo
     */
    private fun processFinish(writeVo: WriteVo) {
        writeVo.startTime = eventCalendarPref.getLong(KEY_START_TIME, Calendar.getInstance().timeInMillis)
        writeVo.endTime = Calendar.getInstance().timeInMillis

        Observable.just(cm.insertEvent(writeVo))
                .subscribe({result ->
                    clearForm()

                    eventCalendarPref.edit()
                            .putString(KEY_STATE, STATE_FINISH)
                            .remove(KEY_CATEGORY)
                            .remove(KEY_TITLE)
                            .remove(KEY_CONTENT)
                            .apply()

                    // 이벤트 목록 최산단에 현재 저장된 이벤트 추가.
                    val eventInfo = EventInfo(java.lang.Long.parseLong(result!!.lastPathSegment))
                    attachEventListView(eventInfo, EventAddPosition.FIRST, 0, {})
                })
    }

    private fun clearForm() {
        // 입력 내용 초기화
        title.setText("")
        content.setText("")

        // 진행 상태. 종료상태로 변경.
        button.setText(R.string.start)
    }


    /**
     * 진행중이라면 저장되어 있는 값들 입력
     * @param enabledCalendarList
     */
    private fun fillContentIfProgressState(enabledCalendarList: List<CalendarInfo>) {
        val stat = eventCalendarPref.getString(KEY_STATE, STATE_FINISH)
        if (STATE_PROGRESS == stat) {
            val id = eventCalendarPref.getLong(KEY_CATEGORY, 0)
            for (i in enabledCalendarList.indices) {
                val info = enabledCalendarList[i]
                if (info.id == id) {
                    category.setSelection(i)
                    break
                }
            }

            title.setText(eventCalendarPref.getString(KEY_TITLE, ""))
            content.setText(eventCalendarPref.getString(KEY_CONTENT, ""))
            button.setText(R.string.finish)
        } else {
            clearForm()
        }
    }

    /**
     * 카테고리 항목 설정
     * @param enabledCalendarList
     */
    private fun setCategoryItem(enabledCalendarList: List<CalendarInfo>) {
        val adapter = ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, enabledCalendarList)
        category.adapter = adapter
    }

    companion object {
        const val EVENT_FILE_NAME = "event"
        const val STATE_PROGRESS = "PROGRESS"
        const val STATE_FINISH = "FINISH"
        const val KEY_STATE = "state"
        const val KEY_CATEGORY = "category"
        const val KEY_TITLE = "title"
        const val KEY_CONTENT = "content"
        const val KEY_START_TIME = "start_time"
        const val MENU_ID_EVENT_MODIFY = 1
        const val EVENT_QUERY_PAGE_SIZE = 10

        fun newInstance() = WriteFragment()
    }
}