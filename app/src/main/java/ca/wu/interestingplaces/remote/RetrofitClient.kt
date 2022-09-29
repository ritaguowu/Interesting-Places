package ca.wu.interestingplaces.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Kotlin makes it easy to declare singletons by object declarations
object RetrofitClient {
    private var retrofit: Retrofit? = null
    fun getClient(baseUrl:String):Retrofit{
        if(retrofit == null){
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}