package io.devholic.epilogue

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.OkHttpClient


object Network {

    val client: OkHttpClient = OkHttpClient()
    val gson: Gson = Gson()
    val jsonMediaType = MediaType.parse("application/json; charset=utf-8")
}
