package com.chaquopy.chaquopy

import androidx.annotation.NonNull
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.*
import android.util.Log

/** ChaquopyPlugin */
class ChaquopyPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "chaquopy")
        channel.setMethodCallHandler(this)
    }

    //  * This will run python code consisting of error and result output...
    fun _runPythonTextCode(code: String): Map<String, Any?> {
        val _returnOutput: MutableMap<String, Any?> = HashMap()
        val _python: Python = Python.getInstance()
        val _console: PyObject = _python.getModule("script")
        val _sys: PyObject = _python.getModule("sys")
        val _io: PyObject = _python.getModule("io")

        return try {
            val _textOutputStream: PyObject = _io.callAttr("StringIO")
            _sys["stdout"] = _textOutputStream
            _console.callAttrThrows("mainTextCode", code)
            _returnOutput["message"] = _textOutputStream.callAttr("getvalue").toString()
            _returnOutput
        } catch (e: PyException) {
            _returnOutput["error"] = e.message.toString()
            _returnOutput
        }
    }

    //  * This will run python code consisting of error and result output...
    fun _runFromFile(base64Code: String, function: String, args: String): Map<String, Any?> {
        val _returnOutput: MutableMap<String, Any?> = HashMap()
        val _python: Python = Python.getInstance()
        val _console: PyObject = _python.getModule("script")
        val _sys: PyObject = _python.getModule("sys")
        val _io: PyObject = _python.getModule("io")

        return try {
            val _textOutputStream: PyObject = _io.callAttr("StringIO")
            _sys["stdout"] = _textOutputStream
            _console.callAttrThrows("mainRunFile", base64Code, function, args)
            _returnOutput["message"] = _textOutputStream.callAttr("getvalue").toString()
            _returnOutput
        } catch (e: PyException) {
            _returnOutput["error"] = e.message.toString()
            _returnOutput
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "runPythonScript" -> {
                try {
                    val code: String = call.arguments() ?: ""
                    val _result = _runPythonTextCode(code)
                    result.success(_result)
                } catch (e: Exception) {
                    val result: MutableMap<String, Any?> = HashMap()
                    result["error"] = e.message.toString()
                    result
                }
            }
            "runFromFile" -> {
                try {
                    val base64String = call.argument("code") ?: ""
                    val function = call.argument("function") ?: ""
                    val args = call.argument("args") ?: ""
                    val _result = _runFromFile(base64String, function, args)
                    result.success(_result)
                } catch (e: Exception) {
                    val result: MutableMap<String, Any?> = HashMap()
                    result["error"] = e.message.toString()
                    result
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
