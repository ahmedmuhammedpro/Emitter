package com.ahmed.emitter

import android.app.Application
import timber.log.Timber

class EmitterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}