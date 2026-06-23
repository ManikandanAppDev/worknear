package com.worknear.app.data.model

enum class TransactionType {
    CREDIT,
    DEBIT
}

data class Transaction(
    val id: String,
    val title: String,
    val date: String,
    val amount: Double,
    val type: TransactionType
)

object WalletData {
    val balance = 2450.00

    val transactions = listOf(
        Transaction("1", "Electrician Service", "20 Jun 2026", 299.0, TransactionType.DEBIT),
        Transaction("2", "Wallet Top-up", "18 Jun 2026", 1000.0, TransactionType.CREDIT),
        Transaction("3", "Plumber Service", "15 Jun 2026", 349.0, TransactionType.DEBIT),
        Transaction("4", "Referral Bonus", "12 Jun 2026", 100.0, TransactionType.CREDIT),
        Transaction("5", "AC Repair Service", "08 Jun 2026", 499.0, TransactionType.DEBIT)
    )
}
