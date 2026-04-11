package com.example.bookkeeping.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bookkeeping.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {

    @Query("SELECT * FROM recurring_rules ORDER BY nextDueDate ASC, id ASC")
    fun observeRecurringRules(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules ORDER BY nextDueDate ASC, id ASC")
    suspend fun getAllRecurringRules(): List<RecurringRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringRule(rule: RecurringRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringRules(rules: List<RecurringRuleEntity>)

    @Update
    suspend fun updateRecurringRule(rule: RecurringRuleEntity)

    @Query("SELECT * FROM recurring_rules WHERE id = :id LIMIT 1")
    suspend fun getRecurringRuleById(id: Long): RecurringRuleEntity?

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    suspend fun deleteRecurringRule(id: Long)

    @Query("DELETE FROM recurring_rules")
    suspend fun clearRecurringRules()
}
