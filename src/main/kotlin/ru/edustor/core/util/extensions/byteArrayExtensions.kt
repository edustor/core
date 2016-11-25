package ru.edustor.core.util.extensions

import org.springframework.util.DigestUtils

fun ByteArray.calculateMD5(): String {
    return DigestUtils.md5DigestAsHex(this)
}