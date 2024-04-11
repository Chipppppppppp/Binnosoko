package io.github.chipppppppppp.binnosoko.util

import android.util.Log

object Logger {
    fun v(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    fun v(tag: String, msg: String?, tr: Throwable?) {
        Log.v(tag, msg, tr)
    }

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    fun d(tag: String, msg: String?, tr: Throwable?) {
        Log.d(tag, msg, tr)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    fun i(tag: String, msg: String?, tr: Throwable?) {
        Log.i(tag, msg, tr)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    fun w(tag: String, msg: String?, tr: Throwable?) {
        Log.w(tag, msg, tr)
    }

    fun w(tag: String, tr: Throwable?) {
        Log.w(tag, tr)
    }

    fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    fun e(tag: String, msg: String?, tr: Throwable?) {
        Log.e(tag, msg, tr)
    }
}