package io.devholic.epilogue.request

import com.google.gson.annotations.SerializedName
import io.devholic.epilogue.entity.SlackMessage


data class SlackMessageRequest(

    @SerializedName("text")
    val message: String
) {

    companion object {
        fun fromEntity(entity: SlackMessage): SlackMessageRequest =
            SlackMessageRequest(entity.message)
    }
}
