package com.john.cena.lifelog.view

import android.view.ContextMenu

interface MyContextMenuInfo : ContextMenu.ContextMenuInfo {
    fun getTag() : Any
}