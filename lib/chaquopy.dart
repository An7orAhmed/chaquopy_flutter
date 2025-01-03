import 'dart:async';

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

  /// This function call the function from the given python file and returns result Map.
  static Future<Map<String, dynamic>> runFromFile({
    required String file,
    String function = 'main',
    String args = '',
  }) async {
    final code = await rootBundle.loadString(file);
    dynamic outputData = await _channel.invokeMethod(
      'runFromFile',
      {'code': code, 'function': function, 'args': args},
    );
    return Map<String, dynamic>.from(outputData);
  }
}
