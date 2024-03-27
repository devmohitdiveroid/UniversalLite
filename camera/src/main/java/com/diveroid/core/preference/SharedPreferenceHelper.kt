package com.diveroid.core.preference

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceHelper (
    context: Context,
    private val prefName: String = "GLOBAL",
    prefMode: Int = Context.MODE_PRIVATE
) {
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(prefName, prefMode)

    fun getPrefName(): String = prefName

    fun getString(key: String, defValue: String? = ""): String {
        return sharedPreferences.getString(key, defValue ?: "").toString()
    }

    fun getInt(key: String, defValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defValue)
    }

    fun getLong(key: String, defValue: Long = -1L): Long {
        return sharedPreferences.getLong(key, defValue)
    }

    fun getFloat(key: String, defValue: Float = 0f): Float {
        return sharedPreferences.getFloat(key, defValue)
    }

    fun getBoolean(key: String, defValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defValue)
    }

    fun putStringValue(key: String, value: String?) {
        sharedPreferences.edit {
            it.putString(key, value)
        }
    }

    fun putInt(key: String, value: Int) {
        sharedPreferences.edit {
            it.putInt(key, value)
        }
    }

    fun putLong(key: String, value: Long) {
        sharedPreferences.edit {
            it.putLong(key, value)
        }
    }

    fun putFloat(key: String, value: Float) {
        sharedPreferences.edit {
            it.putFloat(key, value)
        }
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit {
            it.putBoolean(key, value)
        }
    }

    fun containKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor)->Unit) {
        operation(edit())
        edit().apply()
    }
}