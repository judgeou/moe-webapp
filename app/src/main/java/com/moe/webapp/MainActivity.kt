package com.moe.webapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.conn.DnsResolver
import cz.msebera.android.httpclient.impl.client.HttpClients
import cz.msebera.android.httpclient.impl.conn.SystemDefaultDnsResolver
import java.net.*
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    val hostname = "www.moezone.moe"
    class MimeEncoding constructor(val mime: String, val encoding: String)

    class MoeDnsResolver : DnsResolver {
        private val hostname = "www.moezone.moe"
        private val ip = "154.214.4.67"
        override fun resolve(host: String?): Array<InetAddress> {
            if (hostname.equals(host)) {
                return arrayOf(InetAddress.getByName(ip))
            } else{
                return SystemDefaultDnsResolver.INSTANCE.resolve(host)
            }
        }

    }

    private fun getMimeEncoding (contentType: String): MimeEncoding {
        val values = contentType.split(";")
        val mime = values[0].trim()
        var encoding = ""
        val pattern = Pattern.compile("charset=([a-zA-Z0-9-]+)")

        val matcher = pattern.matcher(contentType)
        if (matcher.find()) {
            encoding = matcher.group(1) as String
        }

        return MimeEncoding(mime, encoding)
    }

    private fun requestWebResourceResponse3 (url: String, requestHeaders: Map<String, String>) : WebResourceResponse {
        val moeDnsResolver = MoeDnsResolver()
        val client = HttpClients.custom().setDnsResolver(moeDnsResolver).build()
        val req = HttpGet(url)

        for ((key, value) in requestHeaders) {
            req.setHeader(key, value)
        }

        val res = client.execute(req)
        val inputStream = res.entity.content
        val ct = getMimeEncoding(res.entity.contentType.value)

        return WebResourceResponse(ct.mime, ct.encoding, inputStream)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val webview = findViewById<WebView>(R.id.moe_webview);
        val web_settings = webview.settings;

        web_settings.javaScriptEnabled = true

        val client = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                request?.let {
                    val url = it.url

                    return super.shouldInterceptRequest(view, request)

                }
                // 对于不需要拦截的请求，正常加载
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val host = request?.url?.host

                if (host == hostname) {
                    return false
                } else {
                    AlertDialog.Builder(view!!.context)
                        .setTitle("跳转到外部")
                        .setMessage("您即将离开萌之领域，跳转到 $host。是否继续？")
                        .setPositiveButton("是") { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request?.url.toString()))
                            view.context.startActivity(intent)
                        }
                        .setNegativeButton("否", null)
                        .show()
                    return true
                }
            }
        }

        webview.webViewClient = client
        webview.setNetworkAvailable(true)
        webview.loadUrl("https://" + hostname)
    }

    override fun onBackPressed() {
        val webview = findViewById<WebView>(R.id.moe_webview);
        if (webview.canGoBack()) {
            webview.goBack()
        } else {
            super.onBackPressed()
        }
    }
}