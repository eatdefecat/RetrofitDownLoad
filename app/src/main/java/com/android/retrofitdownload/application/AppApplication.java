package com.android.retrofitdownload.application;

import android.app.Application;
import android.content.Context;

public class AppApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    // 清除缓存
    public void clear() {
      
    }

    public static Context getContext() {
        return mContext;
    }
}
