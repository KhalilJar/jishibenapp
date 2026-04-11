package com.example.bookkeeping.data.backup

import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.AccountType
import com.example.bookkeeping.data.model.Budget
import com.example.bookkeeping.data.model.BudgetScope
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.RecurringFrequency
import com.example.bookkeeping.data.model.RecurringRule
import org.json.JSONArray
import org.json.JSONObject

object BackupJsonCodec {

    fun encode(snapshot: BackupSnapshot): String {
        return JSONObject()
            .put("version", snapshot.version)
            .put("exportedAt", snapshot.exportedAt)
            .put("records", JSONArray().apply {
                snapshot.records.forEach { record ->
                    put(
                        JSONObject()
                            .put("id", record.id)
                            .put("type", record.type.name)
                            .put("amount", record.amount)
                            .put("tag", record.tag)
                            .put("note", record.note)
                            .put("timestamp", record.timestamp)
                            .put("accountId", record.accountId)
                    )
                }
            })
            .put("accounts", JSONArray().apply {
                snapshot.accounts.forEach { account ->
                    put(
                        JSONObject()
                            .put("id", account.id)
                            .put("name", account.name)
                            .put("type", account.type.name)
                            .put("isArchived", account.isArchived)
                            .put("sortOrder", account.sortOrder)
                    )
                }
            })
            .put("budgets", JSONArray().apply {
                snapshot.budgets.forEach { budget ->
                    put(
                        JSONObject()
                            .put("id", budget.id)
                            .put("yearMonth", budget.yearMonth)
                            .put("scope", budget.scope.name)
                            .put("tag", budget.tag)
                            .put("amountLimit", budget.amountLimit)
                    )
                }
            })
            .put("recurringRules", JSONArray().apply {
                snapshot.recurringRules.forEach { rule ->
                    put(
                        JSONObject()
                            .put("id", rule.id)
                            .put("title", rule.title)
                            .put("type", rule.type.name)
                            .put("amount", rule.amount)
                            .put("tag", rule.tag)
                            .put("note", rule.note)
                            .put("accountId", rule.accountId)
                            .put("frequency", rule.frequency.name)
                            .put("intervalCount", rule.intervalCount)
                            .put("startDate", rule.startDate)
                            .put("nextDueDate", rule.nextDueDate)
                            .put("isActive", rule.isActive)
                    )
                }
            })
            .toString()
    }

    fun decode(json: String): BackupSnapshot {
        val root = JSONObject(json)
        return BackupSnapshot(
            version = root.optInt("version", 1),
            exportedAt = root.optLong("exportedAt", 0L),
            records = root.getJSONArray("records").toRecords(),
            accounts = root.getJSONArray("accounts").toAccounts(),
            budgets = root.getJSONArray("budgets").toBudgets(),
            recurringRules = root.getJSONArray("recurringRules").toRecurringRules()
        )
    }

    private fun JSONArray.toRecords(): List<Record> {
        return buildList(length()) {
            for (index in 0 until length()) {
                val item = getJSONObject(index)
                add(
                    Record(
                        id = item.optLong("id", 0L),
                        type = RecordType.valueOf(item.getString("type")),
                        amount = item.getDouble("amount"),
                        tag = item.getString("tag"),
                        note = item.optString("note").takeIf { it.isNotBlank() },
                        timestamp = item.getLong("timestamp"),
                        accountId = item.optLong("accountId", 1L)
                    )
                )
            }
        }
    }

    private fun JSONArray.toAccounts(): List<Account> {
        return buildList(length()) {
            for (index in 0 until length()) {
                val item = getJSONObject(index)
                add(
                    Account(
                        id = item.optLong("id", 0L),
                        name = item.getString("name"),
                        type = AccountType.valueOf(item.getString("type")),
                        isArchived = item.optBoolean("isArchived", false),
                        sortOrder = item.optInt("sortOrder", index)
                    )
                )
            }
        }
    }

    private fun JSONArray.toBudgets(): List<Budget> {
        return buildList(length()) {
            for (index in 0 until length()) {
                val item = getJSONObject(index)
                add(
                    Budget(
                        id = item.optLong("id", 0L),
                        yearMonth = item.getString("yearMonth"),
                        scope = BudgetScope.valueOf(item.getString("scope")),
                        tag = item.optString("tag").takeIf { it.isNotBlank() },
                        amountLimit = item.getDouble("amountLimit")
                    )
                )
            }
        }
    }

    private fun JSONArray.toRecurringRules(): List<RecurringRule> {
        return buildList(length()) {
            for (index in 0 until length()) {
                val item = getJSONObject(index)
                add(
                    RecurringRule(
                        id = item.optLong("id", 0L),
                        title = item.getString("title"),
                        type = RecordType.valueOf(item.getString("type")),
                        amount = item.getDouble("amount"),
                        tag = item.getString("tag"),
                        note = item.optString("note").takeIf { it.isNotBlank() },
                        accountId = item.optLong("accountId", 1L),
                        frequency = RecurringFrequency.valueOf(item.getString("frequency")),
                        intervalCount = item.optInt("intervalCount", 1),
                        startDate = item.getString("startDate"),
                        nextDueDate = item.getString("nextDueDate"),
                        isActive = item.optBoolean("isActive", true)
                    )
                )
            }
        }
    }
}
