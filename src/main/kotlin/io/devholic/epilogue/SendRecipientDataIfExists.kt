package io.devholic.epilogue

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import io.devholic.epilogue.domain.KATCRepository
import io.devholic.epilogue.domain.MessageRepository
import io.devholic.epilogue.domain.NaverNewsRepository
import io.devholic.epilogue.domain.SlackRepository
import io.devholic.epilogue.enum.NaverNewsCategory
import io.devholic.epilogue.repository.KATCRepositoryImpl
import io.devholic.epilogue.repository.MessageRepositoryImpl
import io.devholic.epilogue.repository.NaverNewsRepositoryImpl
import io.devholic.epilogue.repository.SlackRepositoryImpl
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.io.InputStream
import java.time.LocalDate


class SendRecipientDataIfExists(
    private val katcRepository: KATCRepository =
        KATCRepositoryImpl(),
    private val messageRepository: MessageRepository =
        MessageRepositoryImpl(System.getenv(slackUsername)),
    private val naverNewsRepository: NaverNewsRepository =
        NaverNewsRepositoryImpl(),
    private val slackRepository: SlackRepository =
        SlackRepositoryImpl(
            System.getenv(slackAccessToken),
            System.getenv(slackWebhookUrl)
        )
) : RequestHandler<InputStream, Unit> {

    companion object {
        const val slackAccessToken = "SLACK_ACCESS_TOKEN"
        const val slackChannelId = "SLACK_CHANNEL_ID"
        const val slackWebhookUrl = "SLACK_WEBHOOK_URL"
        const val slackUserId = "SLACK_USER_ID"
        const val slackUsername = "SLACK_USERNAME"
        const val recipientName = "RECIPIENT_NAME"
        const val recipientBirthday = "RECIPIENT_BIRTHDAY"
        const val recipientEnterDate = "RECIPIENT_ENTERDATE"
    }

    private val defaultHeadlineLimit: Int = 10
    private val defaultWriterId: String = ""

    override fun handleRequest(input: InputStream, context: Context): Unit =
        katcRepository.getRecipients(
            System.getenv(recipientName),
            LocalDate.parse(System.getenv(recipientBirthday)),
            LocalDate.parse(System.getenv(recipientEnterDate))
        ).filter {
            it.isNotEmpty()
        }.flatMapSingle { recipients ->
            Single.zip(
                Single.zip(
                    listOf(
                        NaverNewsCategory.IT,
                        NaverNewsCategory.ENTERTAINMENT,
                        NaverNewsCategory.SOCIETY,
                        NaverNewsCategory.WORLD,
                        NaverNewsCategory.LIFE
                    ).map { naverNewsRepository.getHeadlineList(it, defaultHeadlineLimit) },
                    {
                        it.map {
                            @Suppress("UNCHECKED_CAST")
                            it as List<String>
                        }.fold(emptyList<String>(), { acc, result -> acc + result })
                    }
                ),
                slackRepository
                    .getWriterId(
                        System.getenv(slackChannelId),
                        System.getenv(slackUserId),
                        defaultWriterId
                    ),
                BiFunction { newsList: List<String>, id: String ->
                    Triple(recipients, newsList, id)
                }
            )
        }.flatMapCompletable {
            messageRepository.create(
                it.first,
                it.second,
                it.third
            ).flatMapCompletable {
                slackRepository.sendMessage(it)
            }
        }.blockingGet()?.let { throw(it) } ?: Unit
}
