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

    private var console: PyObject? = null
    private var textOutputStream: PyObject? = null

    private fun setupPythonEnv(): Boolean {
        if (console == null || textOutputStream == null) {
            val _python: Python = Python.getInstance()
            val _sys: PyObject = _python.getModule("sys")
            val _io: PyObject = _python.getModule("io")

            console = _python.getModule("App")
            textOutputStream = _io.callAttr("StringIO")
            _sys["stdout"] = textOutputStream

            return true
        }
        return false
    }

    //  * This will run flask app consisting of error and result output...
    fun _startPyServer(port: Int): Map<String, Any?> {
        val _returnOutput: MutableMap<String, Any?> = HashMap()

        return try {
            val isStarted = setupPythonEnv()
            if(isStarted) {
                console?.callAttrThrows("main", port)
                _returnOutput["message"] = textOutputStream?.callAttr("getvalue").toString()
                return _returnOutput
            }
            _returnOutput["message"] = "Python server already running."
            _returnOutput
        } catch (e: PyException) {
            _returnOutput["error"] = e.message.toString()
            _returnOutput
        }
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

    private fun _handleMethodCall(method: String, arguments: Any?, block: (Any?) -> Map<String, Any?>): Map<String, Any?> {
        return try {
            val str: String? = arguments as? String
            val number: Int? = arguments as? Int
            if (str != null) {
                return block(str) 
            } else if (number != null) {
                return block(number) 
            } else {
                return block(arguments) 
            }
        } catch (e: Exception) {
            val result: MutableMap<String, Any?> = HashMap()
            result["error"] = e.message.toString()
            result
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "runPythonScript" -> {
                val code: String = call.arguments() ?: ""
                val _result = _handleMethodCall("runPythonScript", code) { _runPythonTextCode(it!! as String) }
                result.success(_result)
            }
            "startPyServer" -> {
                val port: Int = call.arguments() ?: 5000
                val _result = _handleMethodCall("startPyServer", port) { _startPyServer(it!! as Int) }
                result.success(_result)
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
