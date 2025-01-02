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
