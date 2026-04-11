package com.example.bookkeeping.data.model

enum class AccountType(val displayName: String) {
    CASH("现金"),
    BANK_CARD("银行卡"),
    ALIPAY("支付宝"),
    WECHAT("微信"),
    CUSTOM("自定义")
}
