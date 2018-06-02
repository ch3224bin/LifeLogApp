package com.john.cena.lifelog.calendar

import java.util.*

class DateParam {

    private val term: Term
    private val date: Calendar

    enum class Term private constructor(var field: Int) {
        YEAR(Calendar.YEAR), MONTH(Calendar.MONTH), WEEK(Calendar.WEEK_OF_MONTH), DAY(Calendar.DAY_OF_MONTH)
    }

    init {
        date = Calendar.getInstance()
        term = TERM_DAY
    }

    operator fun next() {
        date.add(term.field, 1)
    }

    fun startTime(): Long {
        return date.timeInMillis
    }

    fun endTime(): Long {
        val endTime = Calendar.getInstance()
        endTime.timeInMillis = date.timeInMillis
        endTime.add(term.field, 1)
        return endTime.timeInMillis
    }

    companion object {

        val TERM_YEAR = Term.YEAR
        val TERM_MONTH = Term.YEAR
        val TERM_WEEK = Term.YEAR
        val TERM_DAY = Term.YEAR
    }

}