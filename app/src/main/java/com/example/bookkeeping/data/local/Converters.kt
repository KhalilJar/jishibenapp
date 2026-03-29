package com.example.bookkeeping.data.local

import androidx.room.TypeConverter
import com.example.bookkeeping.data.model.RecordType

class Converters {
    @TypeConverter
    fun fromRecordType(value: RecordType): String = value.name

    @TypeConverter
    fun toRecordType(value: String): RecordType = RecordType.valueOf(value)
}
