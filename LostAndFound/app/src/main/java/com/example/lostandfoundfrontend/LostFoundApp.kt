package com.example.lostandfoundfrontend

import android.app.Application
import com.example.lostandfoundfrontend.data.TokenStore

class LostFoundApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
    }
}
