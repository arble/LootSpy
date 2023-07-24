package com.lootspy.api

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.lootspy.data.UserStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientSecretBasic
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltWorker
class RefreshTokensTask @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val userStore: UserStore,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val authState = userStore.authState.first()
    val authService = AuthorizationService(context)
    return suspendCoroutine {
      authState.performActionWithFreshTokens(
        authService,
        ClientSecretBasic("AWmnuaM1JS6V2lFlGLn5jLwX8KbY65c-7jIK7VWFZOw")
      ) { accessToken, _, ex ->
        if (ex != null) {
          it.resumeWithException(ex)
        } else {
          val dataBuilder = Data.Builder()
          dataBuilder.putAll(inputData)
          dataBuilder.putString("access_token", accessToken)
          it.resume(Result.success(dataBuilder.build()))
        }
      }
    }
  }
}