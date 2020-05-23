package com.lhc.hook.myapplication;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 星期四.
 * email luohongchao@appiron.com
 */
public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Reflection.unseal(this);

        //开始hook
        ActivityTaskHook taskHook = new ActivityTaskHook(this);
        taskHook.hookService();

    }
}
