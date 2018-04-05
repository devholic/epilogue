package io.devholic.epilogue.domain

import io.devholic.epilogue.entity.Recipient
import io.reactivex.Single
import java.time.LocalDate


interface KATCRepository {

    fun getRecipients(name: String, birthday: LocalDate, enterDate: LocalDate): Single<List<Recipient>>
}
