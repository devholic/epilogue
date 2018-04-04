package io.devholic.epilogue.repository

import io.devholic.epilogue.Network
import io.devholic.epilogue.domain.NaverNewsRepository
import io.devholic.epilogue.enum.NaverNewsCategory
import io.devholic.epilogue.extension.toFormattedString
import io.reactivex.Single
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.*


class NaverNewsRepositoryImpl : NaverNewsRepository {

    private val rankingUrl = "http://m.news.naver.com/rankingList.nhn?sid1=%d&date=%s"
    private val headlineClassname = ".commonlist_tx_headline"

    override fun getHeadlineList(category: NaverNewsCategory, limit: Int): Single<List<String>> =
        Single.fromCallable {
            Network.client
                .newCall(
                    Request.Builder()
                        .url(rankingUrl.format(category.sid1, Date().toFormattedString()))
                        .build()
                ).execute()
        }.map {
            it.use {
                it.body()?.string()?.let {
                    Jsoup.parse(it)
                        .select(headlineClassname)
                        .take(limit)
                        .map { it.text() }
                } ?: emptyList()
            }
        }.retry(3)
}
