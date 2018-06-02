package com.john.cena.lifelog.write

class WriteVo(var categoryId: Long, var title: String?, var content: String?) {
    var eventId: Long = 0
    var startTime: Long = 0
    var endTime: Long = 0
}
