package com.lootspy.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.lootspy.api.RefreshTokensTask

class WorkBuilders {
  companion object {
    fun dispatchUniqueWorker(
      context: Context,
      workerClass: Class<out ListenableWorker>,
      workName: String,
      workData: Map<String, String>? = null,
      policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
    ) {
      val (workManager, workRequest) = getManagerAndInitialRequest(
        context,
        workerClass,
        workData
      )
      workManager.enqueueUniqueWork(workName, policy, workRequest)
    }

    fun dispatchUniqueWorkerLinearFollowers(
      context: Context,
      initialWorkerClass: Class<out ListenableWorker>,
      workName: String,
      workData: Map<String, String>? = null,
      followingJobs: List<Class<out ListenableWorker>>,
      policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP,
      tags: List<String>? = null,
    ) {
      val (workManager, initialRequest) = getManagerAndInitialRequest(
        context,
        initialWorkerClass,
        workData,
        tags
      )
      var chain = workManager.beginUniqueWork(workName, policy, initialRequest)
      for (subsequentClass in followingJobs) {
        val nextRequest = OneTimeWorkRequest.Builder(subsequentClass)
        tags?.forEach { nextRequest.addTag(it) }
        chain = chain.then(nextRequest.build())
      }
      chain.enqueue()
    }

    /**
     * Enqueue a list of Workers, but perform a token refresh if needed before they run.
     */
    fun dispatchUniqueWorkWithTokens(
      context: Context,
      workName: String,
      workData: Map<String, String>?,
      jobs: List<Class<out ListenableWorker>>,
      policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP,
      tags: List<String>? = null,
    ) {
      val workManager = WorkManager.getInstance(context)
      val inputData = Data.Builder()
      if (workData != null) {
        inputData.putAll(workData)
      }
      val tokenRequestBuilder =
        OneTimeWorkRequest.Builder(RefreshTokensTask::class.java).setInputData(inputData.build())
      tags?.forEach { tokenRequestBuilder.addTag(it) }
      var chain = workManager.beginUniqueWork(workName, policy, tokenRequestBuilder.build())
      jobs.forEach { nextClass ->
        val nextRequestBuilder = OneTimeWorkRequest.Builder(nextClass)
        tags?.forEach { nextRequestBuilder.addTag(it) }
        chain = chain.then(nextRequestBuilder.build())
      }
      chain.enqueue()
    }

    private fun getManagerAndInitialRequest(
      context: Context,
      workerClass: Class<out ListenableWorker>,
      workData: Map<String, String>?,
      tags: List<String>? = null,
    ): Pair<WorkManager, OneTimeWorkRequest> {
      val workManager = WorkManager.getInstance(context)
      val data = Data.Builder()
      if (workData != null) {
        data.putAll(workData)
      }
      val workRequest = OneTimeWorkRequest.Builder(workerClass).setInputData(data.build())
      tags?.forEach { workRequest.addTag(it) }
      return Pair(workManager, workRequest.build())
    }
  }
}