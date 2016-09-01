package com.android.retrofitdownload.download;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import rx.Observable;

public interface AppDownloadService {
    @Streaming
    @GET("{url}")
    Observable<ResponseBody> download(@Path("url") String apkName);
}
