package com.android.retrofitdownload.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class AppDownLoadManager {

    public static final int	START          = 1;
    public static final int	PROGRESSING   = 2;
    public static final int COMPLETED      = 3;
    public static final int EXCEPTION      = 4;

    private static AppDownLoadManager mInstance;
    private ConcurrentHashMap<Object, AppDownLoadHelper> mTagToHelpers;
    private Handler mUIHandler;

    private AppDownLoadManager(){
        mTagToHelpers = new ConcurrentHashMap<Object, AppDownLoadHelper>();
        mUIHandler = new UIHandler(mTagToHelpers);
    }

    public synchronized static AppDownLoadManager getInstance() {
        if(mInstance == null) {
            mInstance = new AppDownLoadManager();
        }
        return mInstance;
    }

    public void addHelper(final AppDownLoadHelper helper) {
        if(helper == null) return;

        helper.mListener = new AppProgressListener() {
            @Override
            public void onStart() {
                Message message = mUIHandler.obtainMessage();
                message.obj = helper.mTag;
                message.what = START;
                message.sendToTarget();
            }

            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                Message message = mUIHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putLong("bytesRead", bytesRead);
                bundle.putLong("contentLength", contentLength);
                bundle.putBoolean("done", done);
                message.setData(bundle);
                message.obj = helper.mTag;
                message.what = PROGRESSING;
                message.sendToTarget();
            }

            @Override
            public void onCompleted() {
                Message message = mUIHandler.obtainMessage();
                message.obj = helper.mTag;
                message.what = COMPLETED;
                message.sendToTarget();
            }

            @Override
            public void onError(String err) {
                Message message = mUIHandler.obtainMessage();
                message.obj = helper.mTag;
                message.what = EXCEPTION;
                Bundle bundle = new Bundle();
                bundle.putString("err", err);
                message.setData(bundle);
                message.sendToTarget();
            }
        };

        mTagToHelpers.put(helper.mTag, helper);
    }

    public AppDownLoadHelper getHelperByTag(Object tag) {
        return tag == null? null : mTagToHelpers.get(tag);
    }

    public void cancelHelperByTag(Object tag) {
        AppDownLoadHelper helper = mTagToHelpers.get(tag);

        if(helper != null) {
            helper.setCancel(true);
            helper.mDownloadListeners.clear();
            mTagToHelpers.remove(tag);
        }
    }

    private static class UIHandler extends Handler {

        private ConcurrentHashMap<Object, AppDownLoadHelper> mTagToHelpers;

        public UIHandler(ConcurrentHashMap<Object, AppDownLoadHelper> tagToHelpers){
            mTagToHelpers = tagToHelpers;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            String tagID = (String) msg.obj;
            AppDownLoadHelper helper = mTagToHelpers.get(tagID);
            if(helper == null) return;

            switch(msg.what) {
                case START: //任务开始
                    for(AppProgressListener listener: helper.mDownloadListeners){
                        listener.onStart();
                    }
                    break;

                case PROGRESSING: //任务进度更新
                    Bundle data = msg.getData();
                    for(AppProgressListener listener: helper.mDownloadListeners){
                        listener.update(data.getLong("bytesRead"), data.getLong("contentLength"), data.getBoolean("done"));
                    }
                    break;

                case COMPLETED:   //任务完成
                    for(AppProgressListener listener: helper.mDownloadListeners){
                        listener.onCompleted();
                    }
                    helper.mDownloadListeners.clear();
                    mTagToHelpers.remove(tagID);
                    break;

                case EXCEPTION:   //任务发生异常
                    Bundle e = msg.getData();
                    for(AppProgressListener listener: helper.mDownloadListeners){
                        listener.onError(e.getString("err"));
                    }
                    helper.mDownloadListeners.clear();
                    helper.setCancel(true);
                    mTagToHelpers.remove(tagID);
                    break;

                default:
                    break;
            }
        }
    }

    public void unsubscribe(){
        for (Object key : mTagToHelpers.keySet()) {
            AppDownLoadHelper appDownLoadHelper = mTagToHelpers.get(key);
            appDownLoadHelper.setCancel(true);
            appDownLoadHelper.mDownloadListeners.clear();
            mTagToHelpers.remove(key);
        }
    }
}
