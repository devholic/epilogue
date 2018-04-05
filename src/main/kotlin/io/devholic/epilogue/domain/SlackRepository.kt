package io.devholic.epilogue.domain

import io.devholic.epilogue.entity.SlackMessage
import io.reactivex.Completable
import io.reactivex.Single


interface SlackRepository {

    fun getWriterId(channelId: String, recipientId: String, defaultWriterId: String): Single<String>
    fun sendMessage(message: SlackMessage): Completable
}
