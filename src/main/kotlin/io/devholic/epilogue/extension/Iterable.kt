package io.devholic.epilogue.extension

import java.util.*


fun <T : Any> List<T>.randomPick(): T? =
    Random(System.currentTimeMillis())
        .nextInt(size)
        .let {
            if (isNotEmpty()) this[it] else null
        }
