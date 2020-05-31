package com.lhc.hook.myapplication;

import android.app.Application;
import android.content.Context;

import com.lhc.hook.myapplication.hook.ActivityTaskHook;
import com.lhc.hook.myapplication.hook.HookH;
import com.lhc.hook.myapplication.hookutil.Reflection;

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
        HookH.hook_mH(this);

    }
}
