package io.github.chipppppppppp.binnosoko.hook

import android.text.Spannable
import android.text.SpannableString
import dalvik.system.DexFile
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.Config
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class Chtoio : IHook {
    override fun register(config: Config, lpParam: XC_LoadPackage.LoadPackageParam) {
        if (!config.chtoio) return
        try {
            val apkPath = lpParam.appInfo.sourceDir ?: run {
                
                return
            }

            val dexFile = DexFile(File(apkPath))
            val classNames = dexFile.entries()
            while (classNames.hasMoreElements()) {
                val className = classNames.nextElement()
                try {
                    val clazz = Class.forName(className, false, lpParam.classLoader)
                    hookAllMethods(clazz)
                } catch (e: ClassNotFoundException) {
                    
                } catch (e: Throwable) {
                    
                }
            }
        } catch (e: Throwable) {
            
        }
    }

    private fun hookAllMethods(clazz: Class<*>) {
        val methods = clazz.declaredMethods

        for (method in methods) {
            
            if (Modifier.isAbstract(method.modifiers)) continue

            
            if (method.name != "setText") continue

            
            method.isAccessible = true

            hookMethod(method, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    
                    val argsString = buildString {
                        append("Args: ")
                        for ((i, arg) in param.args.withIndex()) {
                            append("Arg[$i]: ").append(arg ?: "null").append(", ")
                        }
                    }
                    if (param.args.isNotEmpty() && param.args[0] is CharSequence) {
                        val originalText = param.args[0] as CharSequence
                        val textStr = originalText.toString()

                        
                        if (textStr.contains("5ch.net")) {
                            val replacedText = textStr.replace("5ch.net", "5ch.io")
                            when (originalText) {
                                is String -> {
                                    param.args[0] = replacedText
                                    
                                }
                                is Spannable -> {
                                    
                                    val spans = originalText.getSpans(0, originalText.length, Any::class.java)
                                    val newSpannable = SpannableString(replacedText)

                                    for (span in spans) {
                                        val start = originalText.getSpanStart(span)
                                        val end = originalText.getSpanEnd(span)
                                        val flags = originalText.getSpanFlags(span)

                                        
                                        if (start < textStr.length && end <= textStr.length) {
                                            val beforeText = textStr.substring(0, start)
                                            
                                            

                                            
                                            val newStart = beforeText.replace("5ch.net", "5ch.io").length
                                            val newEnd = newStart + (end - start) +
                                                    (replacedText.length - textStr.length)

                                            if (newEnd <= newSpannable.length) {
                                                newSpannable.setSpan(span, newStart, newEnd, flags)
                                            }
                                        }
                                    }

                                    param.args[0] = newSpannable
                                    
                                }
                                else -> {
                                    
                                    param.args[0] = replacedText
                                    
                                }
                            }

                            
                        }
                    }
                }
            })
        }
    }
}