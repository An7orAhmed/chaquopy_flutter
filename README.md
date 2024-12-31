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

### 7. Usage Example in Flutter

Assume your Python script always outputs JSON strings. Here’s how you can invoke it in Flutter:
```dart
import 'dart:convert';
import 'package:chaquopy/chaquopy.dart';

void executePythonScript() async {
  final result = await Chaquopy.executeCode(
      'print("{\\"msg\\": \\"Hello from Python!\\"}")');
  final json = jsonDecode(result['textOutputOrError'].toString().replaceAll("'", "\""));
  print(json["msg"]); // Outputs: Hello from Python!
}
```

---

### Notes
- Ensure Python 8.3 is installed on your system.
- Update the paths and dependencies based on your project’s requirements.
- Verify the `script.py` file is correctly placed under `android/app/src/main/python`.

You are now ready to use Chaquopy with Flutter! 🎉

## FAQs:

1. Why it shows the notification and also crashes the app after some time limit?

    This plugin uses [Chaquopy SDK](https://chaquo.com/chaquopy/), which uses a license for the unlimited use case. In order to remove the notification and timelimit, you need to contact [here](https://chaquo.com/chaquopy/paid-license/) for the license and after you get your license, you need to follow add follwing two lines in your `local.properties` file.

    1. chaquopy.license=`your_license_key`
    2. chaquopy.applicationId=`package name of the app`

2. Can I use python packages?

    You can use all the python packages by using following configuration.
    
    ```
    defaultConfig {
        python {
            pip {
                install "scipy"
                install "numpy"
                //  specify any other package to install.
            }
        }
    }
    ```

3. Why this package doesn't support OpenCV, Matplotlib and NLTK packages?

    I am writing reasons for individual packages here.

    1. OpenCV and Matplotlib : OpenCV and Matplotlib Requires a special configuration, I am working on this issue to be able to integrate in the package itself.
   
    2. NLTK and Spacy : NLTK and Spacy Packages can be installed and technically run on your device, but most of the NLTK and Spacy functionality relies on it's data that you will be downloading using `nltk.download('all')`. so It increases the size of the app significantly. I am also working on this feature.

4. App Size Reduction:
   
   Using ABI Selection settings you can reduce the app size. The Python interpreter is a native component, so you must use the abiFilters setting to specify which ABIs you want the app to support. The currently available ABIs are:

```
    armeabi-v7a, supported by virtually all Android devices.

    arm64-v8a, supported by most recent Android devices.

    x86, for the Android emulator.

    x86_64, for the Android emulator.
```

During development you’ll probably want to enable them all, i.e.:

```
defaultConfig {
    ndk {
       abiFilters "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
    }
}
```

5. buildPython:

Some features require Python 3.5 or later to be available on the build machine. These features are indicated by a note in their documentation sections.

By default, Chaquopy will try to find Python on the PATH with the standard command for your operating system, first with a matching minor version, and then with a matching major version. For example, if Chaquopy’s own Python version is 3.8.x, then:

```
    On Linux and Mac it will try python3.8, then python3.

    On Windows, it will try py -3.8, then py -3.
```

If this doesn’t work for you, set your Python command using the buildPython setting. For example, on Windows you might use one of the following:

```
defaultConfig {
    python {
        buildPython "C:/path/to/python.exe"
        buildPython "C:/path/to/py.exe", "-3.8"
    }
}
```

6. If you're facing Kotlin and Java compatibility issues, you can provide kotlin/java configuration as following: 

```
android {
    compileSdk 34
    namespace 'com.chaquopy.chaquopy_example'

    kotlin {
        jvmToolchain(17)
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
```

7. You might need to update dsitributionUrl for the gradle based on the version you're using, which you can do inside gradle-wrapper.properties. you can find the available gradle urls [here](https://services.gradle.org/distributions/):

```
distributionUrl=URL
```

8. Future Plans : 
   
    [ ] Add support for opencv and matplotlib

    [ ] Add support for NLTK and Spacy

    [ ] Add support for apple devices as well. If you want to help me out with it, kindly [contact]('jayjaydangar96@gmail.com') me,through mail, and I will be happy make this plugin better.


##  Demo : 
    
![](https://user-images.githubusercontent.com/10520025/113665705-94912600-96cb-11eb-8ebd-3732058e52d0.gif)
    
##  Queries : 

All the configurations of chaquopy will work the same way, it's mentioned on the [chaquopy sdk](https://chaquo.com/chaquopy/doc/current/android.html) home page. if you don't find any solution you can open issue in the repository and I am happy to help. :) 
