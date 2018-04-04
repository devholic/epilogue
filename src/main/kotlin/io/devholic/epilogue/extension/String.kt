package io.devholic.epilogue.extension

import java.nio.charset.Charset
import java.util.*


fun String.toBase64(charset: Charset = Charsets.UTF_8): String =
    Base64.getEncoder().encodeToString(this.toByteArray(charset))
