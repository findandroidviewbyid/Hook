package com.lhc.hook.myapplication.hookutil;

import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ServiceHook implements InvocationHandler {
    private static final String TAG = "ServiceHook";

    private IBinder mBase;
    private Class<?> mStub;
    private Class<?> mInterface;
    private InvocationHandler mInvocationHandler;

    public ServiceHook(IBinder mBase, String iInterfaceName, boolean isStub, InvocationHandler InvocationHandler) {
        this.mBase = mBase;
        this.mInvocationHandler = InvocationHandler;

        try {
            this.mInterface = Class.forName(iInterfaceName);
            this.mStub = Class.forName(String.format("%s%s", iInterfaceName, isStub ? "$Stub" : ""));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("queryLocalInterface".equals(method.getName())) {
            try {
                return Proxy.newProxyInstance(proxy.getClass().getClassLoader(), new Class[]{mInterface},
                        new HookHandler(mBase, mStub, mInvocationHandler));
            } catch (Exception e) {

            }
        }
        return method.invoke(mBase, args);
    }

    private static class HookHandler implements InvocationHandler {
        private Object mBase;
        private InvocationHandler mInvocationHandler;

        public HookHandler(IBinder base, Class<?> stubClass,
                           InvocationHandler InvocationHandler) throws Exception {
            mInvocationHandler = InvocationHandler;

            Method asInterface = stubClass.getDeclaredMethod("asInterface", IBinder.class);
            this.mBase = asInterface.invoke(null, base);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (mInvocationHandler != null && mBase != null) {
                return mInvocationHandler.invoke(mBase, method, args);
            }
            Log.e(TAG, "invoke: 66 ");
            return method.invoke(mBase, args);
        }
    }
}