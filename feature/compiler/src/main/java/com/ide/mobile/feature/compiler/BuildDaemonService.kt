package com.ide.mobile.feature.compiler

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Isolated background service running in process=":build_daemon".
 * Shields the main IDE UI process from being killed by Android's Low Memory Killer (LMK)
 * during heavy compilation or memory-intensive DEXing operations.
 */
class BuildDaemonService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }
}
