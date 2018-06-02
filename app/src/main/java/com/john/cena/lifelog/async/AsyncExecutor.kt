package com.john.cena.lifelog.async

import android.os.AsyncTask
import android.util.Log
import java.util.concurrent.Callable

class AsyncExecutor<T> : AsyncTask<Void, Void, T>() {

    private var callback: AsyncCallback<T>? = null
    private var callable: Callable<T>? = null
    private var occuredException: Exception? = null

    private val isExceptionOccured: Boolean
        get() = occuredException != null

    fun setCallable(callable: Callable<T>): AsyncExecutor<T> {
        this.callable = callable
        return this
    }

    fun setCallback(callback: AsyncCallback<T>): AsyncExecutor<T> {
        this.callback = callback
        return this
    }

    override fun doInBackground(vararg params: Void): T? {
        try {
            return callable!!.call()
        } catch (ex: Exception) {
            Log.e(TAG,
                    "exception occured while doing in background: " + ex.message, ex)
            this.occuredException = ex
            return null
        }

    }

    override fun onPostExecute(result: T) {
        if (isCancelled) {
            notifyCanceled()
        }
        if (isExceptionOccured) {
            notifyException()
            return
        }
        notifyResult(result)
    }

    private fun notifyCanceled() {
        if (callback != null)
            callback!!.cancelled()
    }

    private fun notifyException() {
        if (callback != null)
            callback!!.exceptionOccured(occuredException!!)
    }

    private fun notifyResult(result: T) {
        if (callback != null)
            callback!!.onResult(result)
    }

    companion object {
        private val TAG = "AsyncExecutor"
    }
}