package io.devholic.epilogue.entity

import java.time.LocalDate


data class Recipient(
    val birthday: LocalDate,
    val enterDate: LocalDate,
    val name: String,
    val regiment: Int,
    val company: Int,
    val platoon: Int
)
