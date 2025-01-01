## Chaquopy Flutter plugin

This is an unofficial Chaquopy Flutter plugin to run Python code on Android. This is the simplest version, where you can write your code and run it.

This guide provides step-by-step instructions to integrate Chaquopy into your Flutter project.

---

### 1. Install the Chaquopy Dependency
Run the following command to add Chaquopy to your Flutter project:
```bash
flutter pub add chaquopy
```

---

### 2. Update `android/build.gradle`

#### a. Add Chaquopy's Maven Repository
In the `repositories` block, add the following:
```gradle
maven { url "https://chaquo.com/maven" }
```

#### b. Add Chaquopy and Android Build Tools to Dependencies
In the `dependencies` block, add:
```gradle
classpath "com.android.tools.build:gradle:8.7.3"
classpath "com.chaquo.python:gradle:12.0.0"
```

---

### 3. Update `android/local.properties`

Add the following lines:
```properties
chaquopy.license=free
chaquopy.applicationId=com.company.app_name
```

> Replace `com.company.app_name` with your app's package name.

---

### 4. Update `android/app/build.gradle`

#### a. Add the Chaquopy Plugin
In the `plugins` block, add:
```gradle
id "com.chaquo.python"
```

#### b. Update `compileSdkVersion`
Set `compileSdkVersion` to:
```gradle
compileSdkVersion 34
```

#### c. Add Kotlin JVM Toolchain
In the `android` block, add:
```gradle
kotlin {
    jvmToolchain(17)
}
```

#### d. Update `defaultConfig`
Add the following inside the `defaultConfig` block:
```gradle
ndk {
    abiFilters "arm64-v8a", "x86_64"
}
python {
    buildPython "C:\\Python83\\python.exe" // Path to Python 8.3 on your system
    pip {
        install "numpy>=1.17.3"
        install "scikit-learn"
        install "neurokit2"
    }
}
```

> Replace the `buildPython` path with the installation path of Python 8.3 on your system.  
> In the `pip` block, list the Python libraries your project requires.

---

### 5. Modify `AndroidManifest.xml`

Replace or remove the `android:name` attribute in the `<application>` tag:
```xml
android:name="com.chaquo.python.android.PyApplication"
```

---

### 6. Add a Python Script

Create a file named `script.py` in the following directory:
```
android/app/src/main/python
```

#### Content of `script.py`
```python
import io, os, sys, time, threading, ctypes, inspect, traceback

def _async_raise(tid, exctype):
    tid = ctypes.c_long(tid)
    if not inspect.isclass(exctype):
        exctype = type(exctype)
    res = ctypes.pythonapi.PyThreadState_SetAsyncExc(tid, ctypes.py_object(exctype))
    if res == 0:
        raise ValueError("invalid thread id")
    elif res != 1:
        ctypes.pythonapi.PyThreadState_SetAsyncExc(tid, None)
        raise SystemError("Timeout Exception")

def stop_thread(thread):
    _async_raise(thread.ident, SystemExit)
    
def text_thread_run(code):
    try:
        env = {}
        exec(code, env, env)
    except Exception as e:
        print(e)
    
def mainTextCode(code):
    global thread1
    thread1 = threading.Thread(target=text_thread_run, args=(code,), daemon=True)
    thread1.start()
    timeout = 15  # Change timeout settings in seconds here...
    thread1_start_time = time.time()
    while thread1.is_alive():
        if time.time() - thread1_start_time > timeout:
            stop_thread(thread1)
            raise TimeoutError
        time.sleep(1)
```

---

To update your usage example for **Chaquopy** in Flutter for both running Python code and starting a server, I'll show you how to execute a Python script directly and also start a Flask-like server that can handle HTTP requests.

### Flutter Usage Example for Chaquopy

1. **Running Python code**: Use `Chaquopy.executeCode()` to run a Python script.
2. **Starting the server**: Use `Chaquopy.startPyServer()` to start a Python server (like Flask) and send an HTTP request to it.

### Full Example

```dart
import 'dart:convert';
import 'package:chaquopy/chaquopy.dart';
import 'package:http/http.dart' as http;

void executePythonScript() async {
  // Step 1: Running Python code
  final result = await Chaquopy.executeCode(
      'print("{\\"msg\\": \\"Hello from Python!\\"}")');
  final json = jsonDecode(result['textOutputOrError'].toString().replaceAll("'", "\""));
  print(json["msg"]); // Outputs: Hello from Python!
}

Future<void> startFlaskServer() async {
  // Step 2: Starting a Python server
  // This assumes you have a Python server script that can be invoked with Chaquopy
  await Chaquopy.startPyServer(port: 9876); // Start the server on port 9876

  // Step 3: Sending a POST request to the server
  final data = jsonEncode({"data": [1, 2, 3, 4, 5]});

  final resp = await http.post(
    Uri.parse("http://localhost:9876/process"),
    headers: {"Content-Type": "application/json"},
    body: data,
  );

  print("Response from server: ${resp.body}");
}

void main() {
  // First run the script example
  executePythonScript();

  // Then start the server
  startFlaskServer();
}
```

---

### Notes
- Ensure Python 8.3 is installed on your system.
- Update the paths and dependencies based on your project’s requirements.
- Verify the `script.py` file is correctly placed under `android/app/src/main/python`.

You are now ready to use Chaquopy with Flutter! 🎉
