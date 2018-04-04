package io.devholic.epilogue.domain

import io.devholic.epilogue.entity.Recipient
import io.reactivex.Single


interface KATCRepository {

    fun getRecipients(name: String, birthday: String, enterDate: String): Single<List<Recipient>>
}
