package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GoogleSheetsApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(webAppUrl: String): Result<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext Result.failure(Exception("Apps Script URL is empty"))
        }
        try {
            val url = if (webAppUrl.contains("?")) "$webAppUrl&action=ping" else "$webAppUrl?action=ping"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success("Connection Successful! Status 200 OK")
            } else {
                Result.failure(Exception("HTTP ${response.code}: $body"))
            }
        } catch (e: Exception) {
            Log.e("BalajiSheets", "Test connection failed", e)
            Result.failure(e)
        }
    }

    suspend fun syncUserToSheet(
        webAppUrl: String,
        phone: String,
        name: String,
        address: String,
        area: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) return@withContext Result.success(true)
        try {
            val payload = JSONObject().apply {
                put("action", "syncUser")
                put("phone", phone)
                put("name", name)
                put("address", address)
                put("area", area)
            }
            val request = Request.Builder()
                .url(webAppUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Log.e("BalajiSheets", "Sync user failed", e)
            Result.failure(e)
        }
    }

    suspend fun addBookingToSheet(
        webAppUrl: String,
        customerName: String,
        customerPhone: String,
        address: String,
        area: String,
        serviceName: String,
        units: Int,
        modelType: String,
        issueNotes: String,
        couponCode: String,
        discountAmount: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) return@withContext Result.success("LOCAL_ONLY")
        try {
            val payload = JSONObject().apply {
                put("action", "addBooking")
                put("customerName", customerName)
                put("customerPhone", customerPhone)
                put("address", address)
                put("area", area)
                put("serviceName", serviceName)
                put("units", units)
                put("modelType", modelType)
                put("issueNotes", issueNotes)
                put("couponCode", couponCode)
                put("discountAmount", discountAmount)
            }
            val request = Request.Builder()
                .url(webAppUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            Result.success(resStr)
        } catch (e: Exception) {
            Log.e("BalajiSheets", "Add booking to sheet failed", e)
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatusInSheet(
        webAppUrl: String,
        bookingId: String,
        status: String,
        billAmount: Int? = null,
        spareParts: Int? = null,
        technician: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) return@withContext Result.success(true)
        try {
            val payload = JSONObject().apply {
                put("action", "updateBookingStatus")
                put("bookingId", bookingId)
                put("status", status)
                if (billAmount != null) put("billAmount", billAmount)
                if (spareParts != null) put("sparePartsCharge", spareParts)
                if (technician != null) put("technicianName", technician)
            }
            val request = Request.Builder()
                .url(webAppUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Log.e("BalajiSheets", "Update booking in sheet failed", e)
            Result.failure(e)
        }
    }
}
