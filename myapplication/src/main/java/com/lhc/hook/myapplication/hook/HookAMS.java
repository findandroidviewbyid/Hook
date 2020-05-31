package com.lhc.hook.myapplication.hook;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 *
 *
 *
 **/
public class HookAMS {

    private Context context;
    String lastCopyPasteText = "";
    private final String TAG = "HookAMS";

    public HookAMS(Context context) {
        this.context = context;
    }

    public void hookAms() {
        try {

            Field defaultFiled = null;
            Object iActivityManagerObject = null;
            Field mInstance = null;
            if (Build.VERSION.SDK_INT > 28) {
                Class<?> clazz = Class.forName("android.app.ActivityTaskManager");
                defaultFiled = clazz.getDeclaredField("IActivityTaskManagerSingleton");
                defaultFiled.setAccessible(true);
                Object defaultValue = defaultFiled.get(null);

                if (defaultValue == null) {
                }
                //反射SingleTon
                Class<?> SingletonClass = Class.forName("android.util.Singleton");
                mInstance = SingletonClass.getDeclaredField("mInstance");
                mInstance.setAccessible(true);
                iActivityManagerObject = mInstance.get(defaultValue);
                if (iActivityManagerObject != null) {
                    //开始动态代理，用代理对象替换掉真实的ActivityManager，瞒天过海
                    Class<?> IActivityManagerIntercept = Class.forName("android.app.IActivityTaskManager");
                    AmsInvocationHandler handler = new AmsInvocationHandler(iActivityManagerObject);
                    Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{IActivityManagerIntercept}, handler);
                    //现在替换掉这个对象
                    mInstance.set(defaultValue, proxy);
                } else {
                }

            } else if (Build.VERSION.SDK_INT > 25 || (Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT > 0)) {
                Class<?> clazz = Class.forName("android.app.ActivityManager");
                defaultFiled = clazz.getDeclaredField("IActivityManagerSingleton");
                defaultFiled.setAccessible(true);
                Object defaultValue = defaultFiled.get(null);
                //反射SingleTon
                Class<?> SingletonClass = Class.forName("android.util.Singleton");
                mInstance = SingletonClass.getDeclaredField("mInstance");
                mInstance.setAccessible(true);
                iActivityManagerObject = mInstance.get(defaultValue);
                //开始动态代理，用代理对象替换掉真实的ActivityManager，瞒天过海
                Class<?> IActivityManagerIntercept = Class.forName("android.app.IActivityManager");
                AmsInvocationHandler handler = new AmsInvocationHandler(iActivityManagerObject);
                Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{IActivityManagerIntercept}, handler);
                //现在替换掉这个对象
                mInstance.set(defaultValue, proxy);
            } else {
                Class<?> ActivityManagerNativeClss = Class.forName("android.app.ActivityManagerNative");
                defaultFiled = ActivityManagerNativeClss.getDeclaredField("gDefault");
                Log.e("HookUtil", "singleton：123");
                defaultFiled.setAccessible(true);
                Object defaultValue = defaultFiled.get(null);
                //反射SingleTon
                Class<?> SingletonClass = Class.forName("android.util.Singleton");
                Log.e("HookUtil", "singleton：456");
                mInstance = SingletonClass.getDeclaredField("mInstance");
                mInstance.setAccessible(true);
                //到这里已经拿到ActivityManager对象
                iActivityManagerObject = mInstance.get(defaultValue);
                //开始动态代理，用代理对象替换掉真实的ActivityManager，瞒天过海
                Class<?> IActivityManagerIntercept = Class.forName("android.app.IActivityManager");

                AmsInvocationHandler handler = new AmsInvocationHandler(iActivityManagerObject);
                Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{IActivityManagerIntercept}, handler);

                //现在替换掉这个对象
                mInstance.set(defaultValue, proxy);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class AmsInvocationHandler implements InvocationHandler {
        private Object iActivityManagerObject;

        private AmsInvocationHandler(Object iActivityManagerObject) {
            this.iActivityManagerObject = iActivityManagerObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            Log.i("HookUtil", method.getName());
            //我要在这里搞点事情
            if ("startActivity".contains(method.getName())) {
                Intent intent = null;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        intent = (Intent) args[i];
                        index = i;
                        break;
                    }
                }


            }
            return method.invoke(iActivityManagerObject, args);
        }
    }


}