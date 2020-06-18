package com.example.flightmobileapp

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST


interface Api {
    @Headers("Content-Type: application/json")
    @POST("api/Command")
    fun postJoystickData(@Body joystickData: RequestBody): Call<ResponseBody>
}