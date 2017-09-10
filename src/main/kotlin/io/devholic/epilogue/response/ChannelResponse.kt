package io.devholic.epilogue.response

import com.google.gson.annotations.SerializedName
import io.devholic.epilogue.entity.Channel


data class ChannelResponse(

    @SerializedName("members")
    val members: List<String>
)

fun ChannelResponse.toEntity(): Channel = Channel(members)
