package com.lhc.hook.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 *
 *
 */

public class ActivityTaskHook {

    private static final String TAG = ActivityTaskHook.class.getSimpleName();
    private Context context;


    public ActivityTaskHook(Context context) {
        this.context = context;
    }

    public void hookService() {
        IBinder clipboardService = ServiceManager.getService("activity_task");
        String IClipboard = "android.app.IActivityTaskManager";
        try {
            if (clipboardService != null) {
                IBinder hookClipboardService =
                        (IBinder) Proxy.newProxyInstance(IBinder.class.getClassLoader(),
                                new Class[]{IBinder.class},
                                new ServiceHook(clipboardService, IClipboard, true, new ActivityTaskHookHandler()));
                ServiceManager.setService("activity_task", hookClipboardService);
            } else {
                HookAMS hookAMS = new HookAMS(context);
                hookAMS.hookAms();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class ActivityTaskHookHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startActivity".contains(method.getName())) {
                Intent intent = null;
                int intentIndex = 0;
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        intent = (Intent) args[i];
                        intentIndex = i;
                        break;
                    }
                }
                //我们在startactivity中启动的是Main2Activity,这里替换成Main3Activity
                if (intent != null) {
                    Intent main3Intent = new Intent(context, Main3Activity.class);
                    args[intentIndex] = main3Intent;
                }
            }
            return method.invoke(proxy, args);
        }
    }

}