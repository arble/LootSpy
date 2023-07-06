package com.lootspy.api

import com.lootspy.client.ApiClient
import com.lootspy.client.ApiResponse
import okhttp3.Call

inline fun <reified T> ApiClient.executeTyped(call: Call): ApiResponse<T> {
  return execute(call, T::class.java)
}