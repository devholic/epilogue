package io.devholic.epilogue.domain

import io.devholic.epilogue.enum.NaverNewsCategory
import io.reactivex.Single


interface NaverNewsRepository {

    fun getHeadlineList(category: NaverNewsCategory, limit: Int): Single<List<String>>
}
