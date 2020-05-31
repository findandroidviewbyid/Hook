package com.lhc.hook.myapplication.hook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.lhc.hook.myapplication.Main4Activity;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Administrator on 星期六.
 * email luohongchao@appiron.com
 */
public class HookH {
    static Context mContext;
    static  final String TAG  ="HookH";
    public static void hook_mH(Context context) {
        mContext = context;
        try {
            //毫无疑问hookActivityThread
            Class<?> ActivityThreadClz = Class.forName("android.app.ActivityThread");
            Field field = ActivityThreadClz.getDeclaredField("sCurrentActivityThread");
            field.setAccessible(true);
            //获取到当前的进程的ActivityThread
            Object ActivityThreadObj = field.get(null);

            //现在拿mH
            Field mHField = ActivityThreadClz.getDeclaredField("mH");
            mHField.setAccessible(true);
            //获取当前进程的mH
            Handler mHObj = (Handler) mHField.get(ActivityThreadObj);
            //拿到mH的mCallback成员
            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);

            //用我们自己的HandlerCallback代替系统原有的
            ProxyHandlerCallback proxyMHCallback = new ProxyHandlerCallback();//错，不需要重写全部mH，只需要对mH的callback进行重新定义

            //3.替换系统原有的
            mCallbackField.set(mHObj, proxyMHCallback);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProxyHandlerCallback implements Handler.Callback {

        private int EXECUTE_TRANSACTION = 159;

        @Override
        public boolean handleMessage(Message msg) {
            boolean result = false;

            if (msg.what == EXECUTE_TRANSACTION) {//这是跳转的时候,要对intent进行还原
                try {
                    Log.e(TAG, "handleMessage: 66 ");
                    Class<?> ClientTransactionClz = Class.forName("android.app.servertransaction.ClientTransaction");
                    Class<?> LaunchActivityItemClz = Class.forName("android.app.servertransaction.LaunchActivityItem");

                    Field mActivityCallbacksField = ClientTransactionClz.getDeclaredField("mActivityCallbacks");//ClientTransaction的成员
                    mActivityCallbacksField.setAccessible(true);
                    //类型判定，好习惯
                    if (!ClientTransactionClz.isInstance(msg.obj))
                        return true;
                    Object mActivityCallbacksObj = mActivityCallbacksField.get(msg.obj);//根据源码，在这个分支里面,msg.obj就是 ClientTransaction类型,所以，直接用
                    //拿到了ClientTransaction的List<ClientTransactionItem> mActivityCallbacks;
                    List list = (List) mActivityCallbacksObj;

                    if (list.size() == 0)
                        return true;
                    Object LaunchActivityItemObj = list.get(0);//所以这里直接就拿到第一个就好了

                    if (!LaunchActivityItemClz.isInstance(LaunchActivityItemObj))
                        return true;
                    //这里必须判定 LaunchActivityItemClz，
                    // 因为 最初的ActivityResultItem传进去之后都被转化成了这LaunchActivityItemClz的实例

                    Field mIntentField = LaunchActivityItemClz.getDeclaredField("mIntent");
                    mIntentField.setAccessible(true);
                    Intent mIntent = (Intent) mIntentField.get(LaunchActivityItemObj);
                    if (mIntent.getComponent().getClassName().endsWith("com.lhc.hook.myapplication.Main3Activity")) {
                        Intent intent4 = new Intent(mContext, Main4Activity.class);
                        mIntentField.set(LaunchActivityItemObj, intent4);
                    }
                    return result;
                } catch (Exception e) {
                    Log.e(TAG, "handleMessage: 99 ");
                    e.printStackTrace();
                }
            }
            return result;
        }
    }
}
