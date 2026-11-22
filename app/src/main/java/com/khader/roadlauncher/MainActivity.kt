package com.khader.roadlauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : Activity() {
    private lateinit var wv: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.BLACK
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        wv = WebView(this)
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.setBackgroundColor(Color.BLACK)
        wv.addJavascriptInterface(Bridge(), "Android")
        setContentView(wv)
        wv.loadUrl("file:///android_asset/index.html")
    }

    override fun onNewIntent(i: Intent) {
        super.onNewIntent(i)
        wv.evaluateJavascript("window.goHome&&goHome()", null)
    }

    @Deprecated("Launcher stays on screen")
    override fun onBackPressed() {
        wv.evaluateJavascript("window.goHome&&goHome()", null)
    }

    inner class Bridge {
        @JavascriptInterface
        fun getApps(): String {
            val pm = packageManager
            val q = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val list = pm.queryIntentActivities(q, 0)
                .filter { it.activityInfo.packageName != packageName }
                .sortedBy { it.loadLabel(pm).toString().lowercase() }
            val arr = JSONArray()
            for (r in list) {
                val o = JSONObject()
                o.put("name", r.loadLabel(pm).toString())
                o.put("pkg", r.activityInfo.packageName)
                o.put("icon", png(r.loadIcon(pm)))
                arr.put(o)
            }
            return arr.toString()
        }

        @JavascriptInterface
        fun launch(pkg: String) {
            packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
        }

        @JavascriptInterface
        fun pickHome() {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }
    }

    private fun png(d: Drawable): String {
        val bm = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        d.setBounds(0, 0, 96, 96)
        d.draw(Canvas(bm))
        val o = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 90, o)
        return Base64.encodeToString(o.toByteArray(), Base64.NO_WRAP)
    }
}
