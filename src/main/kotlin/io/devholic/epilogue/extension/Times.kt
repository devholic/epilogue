package io.devholic.epilogue.extension

import java.text.SimpleDateFormat
import java.util.*


fun Date.toFormattedString(): String =
    SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(this).toString()
