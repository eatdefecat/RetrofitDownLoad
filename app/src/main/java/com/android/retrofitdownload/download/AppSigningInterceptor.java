package com.android.retrofitdownload.download;

import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AppSigningInterceptor implements Interceptor {

    public AppProgressListener mProgressListener;
    public void setProgressListener(AppProgressListener p){
        mProgressListener = p;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request oldRequest = chain.request();

        String appSecret = "sHqMf0C5mksHfFOi";
        String nonce = String.valueOf(Math.random() * 1000000);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        StringBuilder toSign = new StringBuilder(appSecret).append(nonce).append(timestamp);
        String sign = "";

        // 添加新的参数
        HttpUrl.Builder authorizedUrlBuilder = oldRequest.url()
                .newBuilder()
                .scheme(oldRequest.url().scheme())
                .host(oldRequest.url().host());

        // 设置统一请求参数
        Request.Builder newRequest = oldRequest.newBuilder()
                .method(oldRequest.method(), oldRequest.body())
                // 设置请求头
                .addHeader("AppKey", appSecret)
                .addHeader("Nonce", nonce)
                .addHeader("Signature", sign)
                .addHeader("Timestamp", timestamp)
                .url(authorizedUrlBuilder.build());

        if(mProgressListener != null){
            Response originalResponse = chain.proceed(newRequest.build());
            return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), mProgressListener))
                    .build();
        }
        return chain.proceed(newRequest.build());
    }
}

