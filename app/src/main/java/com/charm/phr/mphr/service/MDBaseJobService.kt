package com.charm.phr.mphr.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.work.Configuration
import com.charm.phr.mphr.util.ModelUtil
import dagger.android.AndroidInjection

abstract class MDBaseJobService : JobService(), LifecycleOwner {

    val mDispatcher = ServiceLifecycleDispatcher(this)

    init {
        Configuration.Builder().setJobSchedulerJobIdRange(ModelUtil.minJobSchedulerJobIdRange, ModelUtil.maxJobSchedulerJobIdRange).build()
    }

    override val lifecycle: Lifecycle
        get() = mDispatcher.lifecycle

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mDispatcher.onServicePreSuperOnStart()
        val returnVal = super.onStartCommand(intent, flags, startId)
        return returnVal
    }

    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        return true
    }

}