package com.example.bookkeeping.data.local

import androidx.room.TypeConverter
import com.example.bookkeeping.data.model.AccountType
import com.example.bookkeeping.data.model.BudgetScope
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.RecurringFrequency

class Converters {
    @TypeConverter
    fun fromRecordType(value: RecordType): String = value.name

    @TypeConverter
    fun toRecordType(value: String): RecordType = RecordType.valueOf(value)

    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun fromBudgetScope(value: BudgetScope): String = value.name

    @TypeConverter
    fun toBudgetScope(value: String): BudgetScope = BudgetScope.valueOf(value)

    @TypeConverter
    fun fromRecurringFrequency(value: RecurringFrequency): String = value.name

    @TypeConverter
    fun toRecurringFrequency(value: String): RecurringFrequency = RecurringFrequency.valueOf(value)
}
