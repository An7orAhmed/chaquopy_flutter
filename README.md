## Chaquopy Flutter plugin

This is an unofficial Chaquopy Flutter plugin to run Python code on Android. This is the simplest version, where you can write your code and run it.

This guide provides step-by-step instructions to integrate Chaquopy into your Flutter project.

---

### 1. Add the Chaquopy Dependency
Add the following lines in `pubspec.yml` to add Chaquopy to your Flutter project:
```yml
chaquopy:
    git:
      ref: master
      url: https://github.com/An7orAhmed/chaquopy_flutter.git
```
then run the following command to fetch:
```bash
flutter pub get
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

### 6. Add a Python Script to run python code

Create a file named `script.py` in the following directory:
```
android/app/src/main/python
```

#### Content of `script.py`
```python
import time
import threading
import ctypes
import inspect

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
    timeout = 15  # change timeout settings in seconds here...
    thread1_start_time = time.time()
    while thread1.is_alive():
        if time.time() - thread1_start_time > timeout:
            stop_thread(thread1)
            raise TimeoutError
        time.sleep(1)

def file_thread_run(code, func_name, func_args):
    try:
        module = {}
        exec(code, module)

        if func_name not in module:
            raise ValueError(f"Function '{func_name}' not found")

        func = module[func_name]
        if callable(func):
            result = func(func_args) if func_args else func()
            print(result)
        else:
            raise ValueError(f"'{func_name}' is not callable")
    except Exception as e:
        print(f"Error in executing function '{func_name}'': {e}")

def mainRunFile(code, func_name, func_args=None):
    global thread2
    thread2 = threading.Thread(target=file_thread_run, args=(code, func_name, func_args,), daemon=True)
    thread2.start()
    timeout = 30  # change timeout settings in seconds here...
    thread2_start_time = time.time()
    while thread2.is_alive():
        if time.time() - thread2_start_time > timeout:
            stop_thread(thread2)
            raise TimeoutError
        time.sleep(1)
```

---

### 7. Flutter Usage Example for Chaquopy

1. **Running Python code**: Use `Chaquopy.executeCode()` to run a Python script.
2. **Calling function from python file**: Use `Chaquopy.runFromFile()` to start a Python file from flutter assets.

### Full Example

```dart
import 'dart:convert';
import 'package:chaquopy/chaquopy.dart';

void executePythonScript() async {
  // Step 1: Running Python code
  final result = await Chaquopy.executeCode(
      'print("{\\"msg\\": \\"Hello from Python!\\"}")');
  final json = jsonDecode(result['textOutputOrError'].toString().replaceAll("'", "\""));
  print(json["msg"]); // Outputs: Hello from Python!
}

Future<void> runPythonFile() async {
  // Step 1: Calling a function from python file
  // This assumes you have a Python file script that is stored in assets folder
  final result = await Chaquopy.runFromFile(
      file: 'assets/test-py.py',
      function: 'process',
      args: List.filled(5000, 1000).toString(),
    );

  if (kDebugMode) print(result); 
}

void main() {
  // First run the script example
  executePythonScript();

  // Then call python function from python file
  runPythonFile();
}
```

---

### Notes
- Ensure Python 8.3 is installed on your system.
- Update the paths and dependencies based on your project’s requirements.
- Verify the `script.py` (given in this repo root dir) file is correctly placed under `android/app/src/main/python` if you want to run python script from flutter.
- Verify the `test-py.py` or your own file is correctly placed under flutter assets folder and also included in `pubspec.yml` file.

You are now ready to use Chaquopy with Flutter! 🎉
