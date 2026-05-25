package com.classplus.player.network

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import java.security.SecureRandom

class TrustAllCerts : X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
}

fun createTrustAllSSLContext(): SSLContext {
    return SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(TrustAllCerts()), SecureRandom())
    }
}