package com.example.bookkeeping.data

import android.content.Context
import android.content.SharedPreferences
import com.example.bookkeeping.data.model.RecordType

object TagDataSource {

    private const val PREFS_NAME = "bookkeeping_tag_prefs"
    private const val KEY_EXPENSE_CUSTOM = "expense_custom_tags"
    private const val KEY_INCOME_CUSTOM = "income_custom_tags"

    val defaultExpenseTags = listOf("吃饭", "购物", "交通", "水电", "娱乐", "居住", "医疗", "其他支出")
    val defaultIncomeTags = listOf("工资", "奖金", "兼职", "报销", "理财", "其他收入")

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun expenseTags(): List<String> =
        (defaultExpenseTags + customExpenseTags()).distinct()

    fun incomeTags(): List<String> =
        (defaultIncomeTags + customIncomeTags()).distinct()

    fun tagsFor(type: RecordType): List<String> = when (type) {
        RecordType.EXPENSE -> expenseTags()
        RecordType.INCOME  -> incomeTags()
    }

    fun defaultTagFor(type: RecordType): String = tagsFor(type).first()

    fun isValidTag(type: RecordType, tag: String): Boolean =
        tag.trim() in tagsFor(type)

    fun isDefaultTag(type: RecordType, tag: String): Boolean = when (type) {
        RecordType.EXPENSE -> tag.trim() in defaultExpenseTags
        RecordType.INCOME  -> tag.trim() in defaultIncomeTags
    }

    fun addTag(type: RecordType, tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isBlank()) return
        if (isValidTag(type, trimmed)) return

        val key = prefsKey(type)
        val current = prefs.getStringSet(key, emptySet<String>())?.toMutableSet() ?: mutableSetOf()
        current.add(trimmed)
        prefs.edit().putStringSet(key, current).apply()
    }

    fun removeTag(type: RecordType, tag: String) {
        val trimmed = tag.trim()
        val key = prefsKey(type)
        val current = prefs.getStringSet(key, emptySet<String>())?.toMutableSet() ?: mutableSetOf()
        if (current.remove(trimmed)) {
            prefs.edit().putStringSet(key, current).apply()
        }
    }

    private fun customExpenseTags(): Set<String> =
        prefs.getStringSet(KEY_EXPENSE_CUSTOM, emptySet<String>()) ?: emptySet<String>()

    private fun customIncomeTags(): Set<String> =
        prefs.getStringSet(KEY_INCOME_CUSTOM, emptySet<String>()) ?: emptySet<String>()

    private fun prefsKey(type: RecordType) = when (type) {
        RecordType.EXPENSE -> KEY_EXPENSE_CUSTOM
        RecordType.INCOME  -> KEY_INCOME_CUSTOM
    }
}


