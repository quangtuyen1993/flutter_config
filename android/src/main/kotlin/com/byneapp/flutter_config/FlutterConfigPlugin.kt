package com.byneapp.flutter_config

import android.content.Context
import android.content.res.Resources
import androidx.annotation.NonNull
import io.flutter.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.lang.reflect.Field

class FlutterConfigPlugin : FlutterPlugin, MethodCallHandler {

  private var applicationContext: Context? = null
  private lateinit var channel: MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    applicationContext = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_config")
    channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    applicationContext = null
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "loadEnvVariables") {
      val variables = loadEnvVariables()
      result.success(variables)
    } else {
      result.notImplemented()
    }
  }

  private fun loadEnvVariables(): Map<String, Any?> {
    val variables = hashMapOf<String, Any?>()

    try {
      val context = applicationContext ?: return emptyMap()
      val resId = context.resources.getIdentifier("build_config_package", "string", context.packageName)
      val className: String = try {
        context.getString(resId)
      } catch (e: Resources.NotFoundException) {
        context.packageName
      }

      val clazz = Class.forName("$className.BuildConfig")

      clazz.declaredFields.forEach {
        variables[it.name] = try {
          it.get(null)
        } catch (e: Exception) {
          null
        }
      }
    } catch (e: ClassNotFoundException) {
      Log.d("FlutterConfig", "Could not access BuildConfig")
    }

    return variables
  }
}
