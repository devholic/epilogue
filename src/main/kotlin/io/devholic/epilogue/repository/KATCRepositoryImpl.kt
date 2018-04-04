package io.devholic.epilogue.repository

import io.devholic.epilogue.Network
import io.devholic.epilogue.domain.KATCRepository
import io.devholic.epilogue.entity.Recipient
import io.devholic.epilogue.enum.HtmlElement
import io.devholic.epilogue.extension.toBase64
import io.reactivex.Single
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element


class KATCRepositoryImpl : KATCRepository {

    private val recipientQueryUrl = "http://www.katc.mil.kr/katc/community/children.jsp"
    private val recipientEnterDateKey = "search_val1" // Enter date (yyyyMMdd) > base64
    private val recipientBirthDayEncodedKey = "search_val2" // Birthday (yyMMdd) > base64
    private val recipientBirthDayKey = "birthDay" // Birthday (yyMMdd)
    private val recipientNameKey = "search_val3" // Name
    private val recipientRegimentIdx = 1
    private val recipientCompanyIdx = 2
    private val recipientPlatoonIdx = 3
    private val recipientId = "[id^=childInfo]"

    private val requiredCellSize = 7

    override fun getRecipients(name: String, birthday: String, enterDate: String): Single<List<Recipient>> =
        Single.fromCallable {
            Network.client
                .newCall(
                    Request.Builder()
                        .url(recipientQueryUrl)
                        .post(buildRecipientQuery(name, birthday, enterDate))
                        .build()
                ).execute()
        }.map {
            it.use {
                it.body()?.string()
                    ?.let {
                        Jsoup.parse(it)
                            .select(recipientId)
                            .map { it.parent().parent().mapRecipient(name, birthday, enterDate) }
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
        select(HtmlElement.TD.value)
            .takeIf { it.size == requiredCellSize }
            ?.let {
                Recipient(
                    birthday,
                    enterDate,
                    name,
                    it[recipientRegimentIdx].text().trim(),
                    it[recipientCompanyIdx].text().trim(),
                    it[recipientPlatoonIdx].text().trim()
                )
            }
}
