package io.devholic.epilogue.response

import com.google.gson.annotations.SerializedName
import io.devholic.epilogue.entity.ChannelInfo


data class ChannelInfoResponse(

    @SerializedName("channel")
    val channel: ChannelResponse
)

fun ChannelInfoResponse.toEntity(): ChannelInfo = ChannelInfo(channel.toEntity())
