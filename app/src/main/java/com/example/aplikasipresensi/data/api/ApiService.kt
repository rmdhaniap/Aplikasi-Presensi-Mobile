package com.example.aplikasipresensi.data.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("api/auth")
    fun login(
        @Field("username") username: String?,
        @Field("password") password: String
    ): Call<JsonObject>?

    @Multipart
    @POST("api/form/upload-image")
    fun uploadImage(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part?
    ): Call<JsonObject?>?

    @FormUrlEncoded
    @POST("api/form/presensi")
    fun absen(
        @Header("Authorization") authorization: String,
        @Field("tipe") tipe: String?,
        @Field("foto") foto: String?,
        @Field("koordinat") koordinat: String?,
        @Field("keterangan") keterangan: String?,
    ): Call<JsonObject>?

    @FormUrlEncoded
    @POST("api/form/presensi")
    fun absenKeluar(
        @Header("Authorization") authorization: String,
        @Field("tipe") tipe: String?,
        @Field("foto") foto: String?,
        @Field("koordinat") koordinat: String?,
        @Field("keterangan") keterangan: String?,
    ): Call<JsonObject>?

    @GET("api/user/riwayat")
    fun riwayat(
        @Header("Authorization") authorization: String,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Call<JsonObject>?
}