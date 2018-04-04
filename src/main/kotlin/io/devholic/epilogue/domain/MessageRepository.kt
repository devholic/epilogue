package io.devholic.epilogue.domain

import io.devholic.epilogue.entity.Recipient
import io.devholic.epilogue.entity.SlackMessage
import io.reactivex.Single


interface MessageRepository {

    fun create(recipients: List<Recipient>, newsList: List<String>, winner: String): Single<SlackMessage>
}
