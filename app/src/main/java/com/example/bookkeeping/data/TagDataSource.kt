package com.example.bookkeeping.data

import com.example.bookkeeping.data.model.RecordType

object TagDataSource {
    val expenseTags = listOf(
        "吃饭",
        "购物",
        "交通",
        "水电",
        "娱乐",
        "居住",
        "医疗",
        "其他支出"
    )

    val incomeTags = listOf(
        "工资",
        "奖金",
        "兼职",
        "报销",
        "理财",
        "其他收入"
    )

    fun tagsFor(type: RecordType): List<String> {
        return when (type) {
            RecordType.INCOME -> incomeTags
            RecordType.EXPENSE -> expenseTags
        }
    }

    fun defaultTagFor(type: RecordType): String {
        return tagsFor(type).first()
    }

    fun isValidTag(type: RecordType, tag: String): Boolean {
        return tag in tagsFor(type)
    }
}
