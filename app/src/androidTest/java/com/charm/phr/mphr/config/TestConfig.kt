package com.charm.phr.mphr.config

import android.content.Context
import java.util.Properties

object TestConfig {

    private lateinit var properties: Properties

    fun init(context: Context) {
        properties = Properties()
        context.assets.open("test_config.properties").use {
            properties.load(it)
        }
    }

    val username: String
        get() = properties.getProperty("test.username", "")

    val password: String
        get() = properties.getProperty("test.password", "")

    val passcode: String
        get() = properties.getProperty("test.passcode", "")
}
