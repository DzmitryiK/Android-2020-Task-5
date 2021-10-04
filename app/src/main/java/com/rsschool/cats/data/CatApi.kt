package com.rsschool.cats.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

interface CatApi {
    @GET("/v1/images/search?api_key=0496c0ab-6557-409a-9215-88218559727e&limit=10")
    suspend fun getListOfCats(): List<Result>
}

object CatApiImpl {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .baseUrl("https://api.thecatapi.com")
        .build()

    private val catsService = retrofit.create(CatApi::class.java)

    suspend fun getListOfCats(lastNumber: Int): List<Cat> {
        return withContext(Dispatchers.IO) {
            var i = lastNumber
            catsService.getListOfCats()
                .map { result ->
                    Cat(
                        i++,
                        result.id,
                        result.url
                    )
                }
        }
    }
}
