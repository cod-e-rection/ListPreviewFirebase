package com.elio.listpreview.helper

import android.content.Context

// Helper class
class HelperClass {

    // func to save value on shared pref
    fun saveBoolean(ctx: Context, key: String?, value: Boolean) {
        val sharedPrefs = ctx.getSharedPreferences("firstSaved", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }
    // load boolean value from shared pref
    fun loadBoolean(ctx: Context, key: String?, defaultValue: Boolean): Boolean {
        val sharedPrefs = ctx.getSharedPreferences("firstSaved", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(key, defaultValue)
    }

}