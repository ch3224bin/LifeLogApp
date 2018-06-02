package com.john.cena.lifelog.write

class WriteVo constructor(var categoryId: Long, var title: String = "", var content: String = "", var startTime: Long = 0) {
    var state: String = ""
    var eventId: Long = 0
    var endTime: Long = 0
}
