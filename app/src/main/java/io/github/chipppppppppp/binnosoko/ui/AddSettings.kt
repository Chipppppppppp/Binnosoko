package io.github.chipppppppppp.binnosoko.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Process
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import io.github.chipppppppppp.binnosoko.R
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.Config
import io.github.chipppppppppp.binnosoko.hook.IHook
import io.github.chipppppppppp.binnosoko.hook.ModuleMain

class AddSettings : IHook {
    private fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    override fun register(config: Config, lpParam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.hookAllMethods(
            lpParam.classLoader.loadClass("jp.syoboi.a2chMate.activity.SettingActivity"),
            "onResume",
            object : XC_MethodHook() {
                @SuppressLint("DiscouragedPrivateApi")
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity

                    val mAddAddAssertPath =
                        AssetManager::class.java.getDeclaredMethod(
                            "addAssetPath",
                            String::class.java
                        )
                    mAddAddAssertPath.isAccessible = true
                    mAddAddAssertPath.invoke(activity.resources.assets, ModuleMain.MODULE_PATH)

                    val prefs = activity.getSharedPreferences(
                        "${ModuleMain.MODULE_NAME}-config",
                        Context.MODE_PRIVATE
                    )

                    val viewGroup = (activity.window.decorView as ViewGroup)

                    val frameLayout = FrameLayout(activity)
                    frameLayout.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val button = Button(activity)
                    button.setText(R.string.app_name)
                    val layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.gravity = Gravity.TOP or Gravity.END
                    layoutParams.rightMargin = dpToPx(10, activity)
                    layoutParams.topMargin = dpToPx(5, activity)
                    button.layoutParams = layoutParams

                    val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.app_name))
                        .setCancelable(false)

                    val layout = LinearLayout(activity)
                    layout.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    layout.orientation = LinearLayout.VERTICAL
                    layout.setPadding(
                        dpToPx(20, activity),
                        dpToPx(20, activity),
                        dpToPx(20, activity),
                        dpToPx(20, activity)
                    )

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dpToPx(20, activity)
                    }

                    val hideAdSwitch = Switch(activity).apply {
                        setText(R.string.settings_remove_ads_title)
                        setLayoutParams(params)
                        isChecked = config.hideAd
                    }
                    layout.addView(hideAdSwitch)

                    layout.addView(TextView(activity).apply {
                        setText(R.string.settings_class_ad_parent_title)
                        setLayoutParams(params)
                    })
                    val adParentClassEditText = EditText(activity).apply {
                        text.insert(0, config.adParentClass)
                    }
                    layout.addView(adParentClassEditText)

                    val replaceUserAgentSwitch = Switch(activity).apply {
                        setText(R.string.settings_replace_ua_title)
                        setLayoutParams(params)
                        isChecked = config.replaceUserAgent
                    }
                    layout.addView(replaceUserAgentSwitch)

                    layout.addView(TextView(activity).apply {
                        setText(R.string.settings_ua_title)
                        setLayoutParams(params)
                    })
                    val userAgentEditText = EditText(activity).apply {
                        text.insert(0, config.userAgent)
                    }
                    layout.addView(userAgentEditText)

                    val removeMonaKeySwitch = Switch(activity).apply {
                        setText(R.string.settings_remove_api_id_title)
                        setLayoutParams(params)
                        isChecked = config.removeMonaKey
                    }
                    layout.addView(removeMonaKeySwitch)

                    layout.addView(TextView(activity).apply {
                        setText(R.string.settings_class_cookie_title)
                        setLayoutParams(params)
                    })
                    val cookieClassEditText = EditText(activity).apply {
                        text.insert(0, config.cookieClass)
                    }
                    layout.addView(cookieClassEditText)

                    layout.addView(TextView(activity).apply {
                        setText(R.string.settings_pref_api_name_title)
                        setLayoutParams(params)
                    })
                    val prefMonaKeyFileEditText = EditText(activity).apply {
                        text.insert(0, config.prefMonaKeyFile)
                    }
                    layout.addView(prefMonaKeyFileEditText)

                    layout.addView(TextView(activity).apply {
                        setText(R.string.settings_pref_api_id_key_title)
                        setLayoutParams(params)
                    })
                    val prefMonaKeyNameEditText = EditText(activity).apply {
                        text.insert(0, config.prefMonaKeyName)
                    }
                    layout.addView(prefMonaKeyNameEditText)

                    val scrollView = ScrollView(activity)
                    scrollView.addView(layout)
                    builder.setView(scrollView)

                    builder.setPositiveButton(
                        R.string.positive
                    ) { _, _ ->
                        val configCopy = Config(
                            hideAd = hideAdSwitch.isChecked,
                            adParentClass = adParentClassEditText.text.toString(),
                            replaceUserAgent = replaceUserAgentSwitch.isChecked,
                            userAgent = userAgentEditText.text.toString(),
                            removeMonaKey = removeMonaKeySwitch.isChecked,
                            cookieClass = cookieClassEditText.text.toString(),
                            prefMonaKeyFile = prefMonaKeyFileEditText.text.toString(),
                            prefMonaKeyName = prefMonaKeyNameEditText.text.toString()
                        )
                        prefs.edit()
                            .putBoolean("hideAd", hideAdSwitch.isChecked)
                            .putString("adHeight", adParentClassEditText.text.toString())
                            .putBoolean("replaceUserAgent", replaceUserAgentSwitch.isChecked)
                            .putString("userAgent", userAgentEditText.text.toString())
                            .putBoolean("removeMonaKey", removeMonaKeySwitch.isChecked)
                            .putString("cookieClass", cookieClassEditText.text.toString())
                            .putString("prefMonaKeyFile", prefMonaKeyFileEditText.text.toString())
                            .putString("prefMonaKeyName", prefMonaKeyNameEditText.text.toString())
                            .commit()
                        if (config != configCopy) {
                            Toast.makeText(
                                activity.applicationContext,
                                activity.getString(R.string.restarting),
                                Toast.LENGTH_SHORT
                            ).show()
                            Process.killProcess(Process.myPid())
                            activity.startActivity(
                                Intent().setClassName(
                                    ModuleMain.MODULE_NAME,
                                    "jp.syoboi.a2chMate.activity.HomeActivity"
                                )
                            )
                        }
                    }

                    val dialog = builder.create()

                    button.setOnClickListener { dialog.show() }

                    frameLayout.addView(button)
                    viewGroup.addView(frameLayout)
                }
            })
    }
}