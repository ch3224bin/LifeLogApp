package com.john.cena.lifelog.view

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.widget.LinearLayout

class MyLinearLayout : LinearLayout {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo {
        return object:MyContextMenuInfo {
            override fun getTag(): Any {
                return this@MyLinearLayout.tag
            }
        }
    }

}