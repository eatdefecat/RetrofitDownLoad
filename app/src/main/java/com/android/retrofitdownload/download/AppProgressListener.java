package com.android.retrofitdownload.download;

public interface AppProgressListener {
    void onStart();
    void update(long bytesRead, long contentLength, boolean done);
    void onCompleted();
    void onError(String err);
}
