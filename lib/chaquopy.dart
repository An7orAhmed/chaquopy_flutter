import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter/services.dart';

/// static class for accessing the executeCode function.
class Chaquopy {
  static const MethodChannel _channel = const MethodChannel('chaquopy');

  /// This function execute your python code and returns result Map.
  /// Structure of result map is :
  /// result['textOutput'] : The original output / error
  static Future<Map<String, dynamic>> executeCode(String code) async {
    dynamic outputData = await _channel.invokeMethod('runPythonScript', code);
    return Map<String, dynamic>.from(outputData);
  }

  /// This function execute App.py, start a HTTP server on localhost and returns result Map.
  static Future<Map<String, dynamic>> runFromFile({
    required String file,
    required String function,
    String args = '',
  }) async {
    final code = await rootBundle.load(file);
    final fileBytes = code.buffer.asUint8List();
    String base64String = base64Encode(fileBytes);
    dynamic outputData = await _channel.invokeMethod(
      'runFromFile',
      {'code': base64String, 'function': function, 'args': args},
    );
    return Map<String, dynamic>.from(outputData);
  }
}
