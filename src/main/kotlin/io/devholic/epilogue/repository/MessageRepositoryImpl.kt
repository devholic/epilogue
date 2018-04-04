package io.devholic.epilogue.repository

import io.devholic.epilogue.domain.MessageRepository
import io.devholic.epilogue.entity.Recipient
import io.devholic.epilogue.entity.SlackMessage
import io.devholic.epilogue.extension.randomPick
import io.reactivex.Single


class MessageRepositoryImpl(slackUsername: String) : MessageRepository {

    private val contentPrefixes: List<String> =
        listOf(
            "안녕하세오 <@$slackUsername> 이에오",
            "훈련소간 <@$slackUsername> 봇으로 또 왔네",
            "안드로이드팀의 ~귀요미~ <@$slackUsername> 이에오",
            "솔찍히 여러분 저 보고싶어하시는거 다 알고 왔읍니다",
            "맞지맞지 내말맞지 나보고싶지 :pepe:",
            "레하~ (레이니스트 하이라는 뜻) :doge:"
        )

    override fun create(recipients: List<Recipient>, newsList: List<String>, winner: String): Single<SlackMessage> =
        Single.fromCallable {
            SlackMessage(
                "${contentPrefixes.randomPick()}\n" +
                    "<@$winner>님이 편지 써주면 얼마나 좋을까 :pepe-sad2:\n" +
                    "(솔직히 겜블보다 이게 더 짜릿하지 않나용 :pepe:)\n" +
                    "편지는 http://www.katc.mil.kr 에서 쓰실 수 있어용 :doge:\n" +
                    if (recipients.size > 1) {
                        "아! 동명이인이 있사오니 확인하시고 써주시면 감사하겠습니당 (이 중에 한명이에용 :doge:)\n" +
                            recipients
                                .map { formatRecipient(it) }
                                .joinToString(separator = "\n")
                    } else recipients.first().let { formatRecipient(it) } +
                        "\n감사합니당!\n" +
                        "(쓸 내용이 없으시다면 아래 뉴스라도 보내주시면 감사하겠읍니다... :pepe-dance:)\n" +
                        "```\n${newsList.joinToString(separator = " /")}\n```"
            )
        }

    private fun formatRecipient(recipient: Recipient): String =
        "> 생년월일 ${recipient.birthday} 입영일 ${recipient.enterDate} / " +
            "${recipient.regiment} 연대 ${recipient.company} 중대 ${recipient.platoon} 소대"
}
