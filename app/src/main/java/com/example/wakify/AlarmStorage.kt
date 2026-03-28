package com.example.wakify

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object AlarmStorage {

    private const val PREFS_NAME = "wakify_alarms"
    private const val KEY_ALARMS = "alarms"
    private const val KEY_NEXT_REQUEST_CODE = "next_request_code"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAlarms(context: Context, alarms: List<Alarm>) {
        val jsonArray = JSONArray()
        for (alarm in alarms) {
            val obj = JSONObject().apply {
                put("hour", alarm.hour)
                put("minute", alarm.minute)
                put("requestCode", alarm.requestCode)
            }
            jsonArray.put(obj)
        }
        prefs(context).edit().putString(KEY_ALARMS, jsonArray.toString()).apply()
    }

    fun loadAlarms(context: Context): MutableList<Alarm> {
        val json = prefs(context).getString(KEY_ALARMS, null) ?: return mutableListOf()
        val jsonArray = JSONArray(json)
        val list = mutableListOf<Alarm>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                Alarm(
                    hour = obj.getInt("hour"),
                    minute = obj.getInt("minute"),
                    requestCode = obj.getInt("requestCode")
                )
            )
        }
        return list
    }

    fun saveNextRequestCode(context: Context, code: Int) {
        prefs(context).edit().putInt(KEY_NEXT_REQUEST_CODE, code).apply()
    }

    fun loadNextRequestCode(context: Context): Int {
        return prefs(context).getInt(KEY_NEXT_REQUEST_CODE, 1)
    }
}
