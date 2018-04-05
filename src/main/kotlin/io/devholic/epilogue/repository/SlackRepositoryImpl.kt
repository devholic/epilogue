package io.devholic.epilogue.repository

import io.devholic.epilogue.Network
import io.devholic.epilogue.domain.SlackRepository
import io.devholic.epilogue.entity.SlackMessage
import io.devholic.epilogue.extension.randomlyPick
import io.devholic.epilogue.request.SlackMessageRequest
import io.devholic.epilogue.response.ChannelInfoResponse
import io.devholic.epilogue.response.toEntity
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.RequestBody


class SlackRepositoryImpl(
    private val accessToken: String,
    private val webhookUrl: String
) : SlackRepository {

    private val slackChannelApiUrl = "https://slack.com/api/channels.info"
    private val tokenKey = "token"
    private val channelKey = "channel"

    override fun getWriterId(
        channelId: String,
        recipientId: String,
        defaultWriterId: String
    ): Single<String> =
        Single.fromCallable {
            Network.client.newCall(
                Request.Builder()
                    .url(slackChannelApiUrl)
                    .post(
                        FormBody.Builder()
                            .add(tokenKey, accessToken)
                            .add(channelKey, channelId)
                            .build()
                    )
                    .build()
            ).execute()
                .use {
                    it.body()?.let {
                        Network.gson.fromJson(it.string(), ChannelInfoResponse::class.java)
                            .toEntity()
                            .channel
                            .members
                            .filter { it != recipientId }
                            .toMutableList()
                            .randomlyPick()
                    } ?: defaultWriterId
                }
        }.retry(3)

    override fun sendMessage(message: SlackMessage): Completable =
        Completable.fromCallable {
            Network.client.newCall(
                Request.Builder()
                    .url(webhookUrl)
                    .post(
                        RequestBody.create(
                            Network.jsonMediaType,
                            Network.gson.toJson(SlackMessageRequest.fromEntity(message))
                        )
                    )
                    .build()
            ).execute().close()
        }.retry(3)
}
