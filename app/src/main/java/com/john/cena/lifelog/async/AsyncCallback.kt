package com.john.cena.lifelog.async

interface AsyncCallback<T> {
    fun onResult(result: T)
    fun exceptionOccured(e: Exception)
    fun cancelled()

    abstract class Base<T> : AsyncCallback<T> {

        override fun exceptionOccured(e: Exception) {}

        override fun cancelled() {}

    }
}