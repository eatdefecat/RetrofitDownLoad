package com.android.retrofitdownload.download;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AppDownLoadHelper {

    public static final int CONNECTION_TIMEOUT = 10;

    public static final int READ_DOWN_TIMEOUT = 20;

    public static final String DOWNLOAD_BASE_URL = "http://dl.play.91.com/bigdata/";

    public Set<AppProgressListener> mDownloadListeners;

    private AppDownLoadManager mManager;

    public AppProgressListener mListener;

    private String mUrl;

    private String mPath;

    private int mConnectionTimeout;

    private int mReadTimeout;

    public Object mTag;

    private Retrofit mAdapter;

    private AppDownloadService mUploadService;

    private boolean isCancel;

    private AppDownLoadHelper() {
        mDownloadListeners = new HashSet<>();
        mManager = AppDownLoadManager.getInstance();
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public void registerListener(AppProgressListener listener) {
        mDownloadListeners.add(listener);
    }

    public void unRegisterListener(AppProgressListener listener) {
        mDownloadListeners.remove(listener);
    }

    private OkHttpClient getDefaultOkHttp() {
        return getBuilder().build();
    }

    private OkHttpClient.Builder getBuilder() {
        AppSigningInterceptor signingInterceptor = new AppSigningInterceptor();
        signingInterceptor.setProgressListener(mListener);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(signingInterceptor);
        builder.connectTimeout(mConnectionTimeout, TimeUnit.SECONDS);
        builder.readTimeout(mReadTimeout, TimeUnit.SECONDS);
        return builder;
    }

    public static class Builder {
        private AppProgressListener mListener;
        private String mUrl;
        private String mPath;
        private int mConnectionTimeout;
        private int mReadTimeout;
        private Object mTag;

        public Builder() {
            this.mConnectionTimeout = CONNECTION_TIMEOUT;
            this.mReadTimeout = READ_DOWN_TIMEOUT;
        }

        /**
         * 设置任务标记
         */
        public Builder setTag(Object tag) {
            this.mTag = tag;
            return this;
        }

        /**
         * 设置下载文件的URL
         */
        public Builder setUrl(String url) {
            this.mUrl = url;
            return this;
        }

        /**
         * 设置HTTP请求连接超时时间，默认10s
         */
        public Builder setConnectionTimeout(int timeout) {
            this.mConnectionTimeout = timeout;
            return this;
        }

        /**
         * 设置HTTP请求数据读取超时时间，默认20s
         */
        public Builder setReadTimeout(int timeout) {
            this.mReadTimeout = timeout;
            return this;
        }

        /**
         * 设置下载文件保存地址，使用绝对路径
         */
        public Builder setPath(String path) {
            this.mPath = path;
            return this;
        }

        /**
         * 设置下载监听
         */
        public Builder setDownLoadListener(AppProgressListener listener) {
            this.mListener = listener;
            return this;
        }

        /**
         * 创建一个本地任务
         */
        public AppDownLoadHelper create() {
            final AppDownLoadHelper helper = new AppDownLoadHelper();
            helper.mConnectionTimeout = mConnectionTimeout;
            helper.mReadTimeout = mReadTimeout;
            helper.mPath = mPath;
            helper.mTag = mTag;
            helper.mUrl = mUrl;
            if(mListener != null) {
                helper.mDownloadListeners.add(mListener);
            }
            return helper;
        }

    }

    public void execute() {
        mManager.addHelper(this);

        mAdapter = new Retrofit.Builder()
                .baseUrl(DOWNLOAD_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(getDefaultOkHttp())
                .build();

        mUploadService = mAdapter.create(AppDownloadService.class);

        final long startTime = System.currentTimeMillis();
        Subscription subscribe = mUploadService.download(mUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseBody>() {

                    @Override
                    public void onCompleted() {
                        if (isCancel) {
                            mListener.onError("cancel");
                        } else if (mListener != null) {
                            mListener.onCompleted();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mListener != null) {
                            mListener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        Log.i("tag", "========next" + responseBody.contentLength());

                        OutputStream output = null;
                        InputStream input = null;
                        File ret;
                        try {
                            input = responseBody.byteStream();
                            ret = new File(mPath);
                            output = new FileOutputStream(ret);
                            byte[] buffer = new byte[8192];
                            int len = -1;
                            while ((len = input.read(buffer)) != -1) {
                                if (isCancel){
                                    ret.delete();
                                    return;
                                }
                                output.write(buffer, 0, len);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (input != null) {
                                    input.close();
                                }
                                if (output != null) {
                                    output.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        long endTime = System.currentTimeMillis();
                        Log.i("tag", "========time" + (endTime - startTime));
                    }
                });

        if(mListener != null) {
            mListener.onStart();
        }
    }
}