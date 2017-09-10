package io.devholic.epilogue.repository

import io.devholic.epilogue.Network
import io.devholic.epilogue.entity.SlackMessage
import io.devholic.epilogue.extension.randomPick
import io.devholic.epilogue.request.SlackMessageRequest
import io.devholic.epilogue.response.ChannelInfoResponse
import io.devholic.epilogue.response.toEntity
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.RequestBody


class SlackRepository(
    private val accessToken: String,
    private val webhookUrl: String
) {

    fun getWriterId(
        channelId: String,
        recipientUsername: String,
        defaultWriterUsername: String = "slackbot"
    ): Single<String> =
        Single.fromCallable {
            Network.client.newCall(
                Request.Builder()
                    .url("https://slack.com/api/channels.info")
                    .post(
                        FormBody.Builder()
                            .add("token", accessToken)
                            .add("channel", channelId)
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
                            .filter { it != recipientUsername }
                            .toMutableList()
                            .randomPick()
                    } ?: defaultWriterUsername
                }
        }.retry(3)

    fun send(message: SlackMessage): Completable =
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
