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
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class KATCRepositoryImpl : KATCRepository {

    private val nonNumberRegex = "\\D".toRegex()

    private val birthdayFormatter = DateTimeFormatter.ofPattern("yyMMdd")
    private val enterDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    private val recipientQueryUrl = "http://www.katc.mil.kr/katc/community/children.jsp"
    private val recipientEnterDateKey = "search_val1"
    private val recipientEncodedBirthDayKey = "search_val2"
    private val recipientBirthDayKey = "birthDay"
    private val recipientNameKey = "search_val3"
    private val recipientRegimentIdx = 1
    private val recipientCompanyIdx = 2
    private val recipientPlatoonIdx = 3
    private val recipientId = "[id^=childInfo]"

    private val requiredCellSize = 7

    /**
     * 육군훈련소 홈페이지에서 편지를 받을 훈련병을 검색합니다.
     *
     * @param name 훈련병 이름
     * @param birthday 훈련병 생년월일
     * @param enterDate 훈련병 입소일
     */
    override fun getRecipients(name: String, birthday: LocalDate, enterDate: LocalDate): Single<List<Recipient>> =
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
                            .fold(
                                emptyList<Recipient>(),
                                { acc, r ->
                                    r.parent().parent()
                                        .mapRecipient(name, birthday, enterDate)
                                        ?.let {
                                            acc + it
                                        } ?: acc
                                }
                            )
                    } ?: emptyList()
            }
        }.retry(3)

    /**
     * 훈련병을 검색을 위한 FormBody를 생성합니다.
     *
     * @param name 훈련병 이름
     * @param birthday 훈련병 생년월일 (yyMMdd)
     * @param enterDate 훈련병 입소일 (yyyyMMdd)
     */
    private fun buildRecipientQuery(name: String, birthday: LocalDate, enterDate: LocalDate): FormBody {
        val formattedBirthday = birthday.format(birthdayFormatter)
        val formattedEnterDate = enterDate.format(enterDateFormatter)

        return FormBody.Builder()
            .add(recipientNameKey, name)
            .add(recipientBirthDayKey, formattedBirthday)
            .add(recipientEncodedBirthDayKey, formattedBirthday.toBase64())
            .add(recipientEnterDateKey, formattedEnterDate.toBase64())
            .build()
    }

    private fun Element.mapRecipient(name: String, birthday: LocalDate, enterDate: LocalDate): Recipient? =
        select(HtmlElement.TD.value)
            .takeIf { it.size == requiredCellSize }
            ?.let {
                Recipient(
                    birthday,
                    enterDate,
                    name,
                    it[recipientRegimentIdx].text().replace(nonNumberRegex, "").toInt(),
                    it[recipientCompanyIdx].text().replace(nonNumberRegex, "").toInt(),
                    it[recipientPlatoonIdx].text().replace(nonNumberRegex, "").toInt()
                )
            }
}
