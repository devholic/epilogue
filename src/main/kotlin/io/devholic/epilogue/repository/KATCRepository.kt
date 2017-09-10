package io.devholic.epilogue.repository

import io.devholic.epilogue.Network
import io.devholic.epilogue.entity.Recipient
import io.devholic.epilogue.extension.toBase64
import io.reactivex.Single
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element


class KATCRepository {

    private val recipientEnterDateKey = "search_val1" // Enter date (yyyyMMdd) > base64
    private val recipientBirthDayEncodedKey = "search_val2" // Birthday (yyMMdd) > base64
    private val recipientBirthDayKey = "birthDay" // Birthday (yyMMdd)
    private val recipientNameKey = "search_val3" // Name
    private val recipientIdIdx = 2
    private val recipientRegimentIdx = 4
    private val recipientCompanyIdx = 5
    private val recipientPlatoonIdx = 6
    private val recipientId = "[id^=childInfo]"
    private val recipientFilterAttr = "onclick"
    private val recipientRegex = "check\\((.*)\\);".toRegex()
    private val recipientNormalizeRegex = "\\s|'".toRegex()

    fun getRecipients(name: String, birthday: String, enterDate: String): Single<List<Recipient>> =
        Single.fromCallable {
            Network.client
                .newCall(
                    Request.Builder()
                        .url("http://www.katc.mil.kr/katc/community/children.jsp")
                        .post(buildRecipientQuery(name, birthday, enterDate))
                        .build()
                ).execute()
        }.map {
            it.use {
                it.body()?.string()
                    ?.let {
                        Jsoup.parse(it)
                            .select(recipientId)
                            .map { it.mapRecipient(name, birthday, enterDate) }
                            .filter { it != null }
                            .map { it!! }
                    } ?: emptyList<Recipient>()
            }
        }.retry(3)

    private fun buildRecipientQuery(name: String, birthday: String, enterDate: String): FormBody =
        FormBody.Builder()
            .add(recipientNameKey, name)
            .add(recipientBirthDayKey, birthday)
            .add(recipientBirthDayEncodedKey, birthday.toBase64())
            .add(recipientEnterDateKey, enterDate.toBase64())
            .build()

    private fun Element.mapRecipient(name: String, birthday: String, enterDate: String): Recipient? =
        recipientRegex.find(attr(recipientFilterAttr))
            ?.takeIf { it.groups.size >= 2 }
            ?.let {
                it.groups[1]!!.value
                    .replace(recipientNormalizeRegex, "")
                    .split(",")
                    .let {
                        if (it.size == 7) {
                            Recipient(
                                it[recipientIdIdx],
                                birthday,
                                enterDate,
                                name,
                                it[recipientRegimentIdx],
                                it[recipientCompanyIdx],
                                it[recipientPlatoonIdx]
                            )
                        } else null
                    }
            }
}
