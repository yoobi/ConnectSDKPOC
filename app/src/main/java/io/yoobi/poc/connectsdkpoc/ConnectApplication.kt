package io.yoobi.poc.connectsdkpoc

import android.app.Application
import com.connectsdk.discovery.DiscoveryManager

class ConnectApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        DiscoveryManager.init(this)
    }
}