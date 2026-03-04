package ru.createsmart.artopos.core.common.util

fun String?.clearText(): String {
    return this
        ?.replace("\r\n", " ")
        ?.replace("painting proper:", "")
        ?.trim()
        ?: ""
}
