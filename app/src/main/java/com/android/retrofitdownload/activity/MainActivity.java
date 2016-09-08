package com.android.retrofitdownload.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.android.retrofitdownload.application.AppApplication;
import com.android.retrofitdownload.download.AppDownLoadHelper;
import com.android.retrofitdownload.download.AppDownLoadManager;
import com.android.retrofitdownload.download.AppProgressListener;
import com.android.retrofitdownload.R;
import com.android.retrofitdownload.utils.SDCardUtil;

public class MainActivity extends AppCompatActivity {

    private Button mDownButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDownButton = (Button) findViewById(R.id.main_button_down);
        mDownButton.setOnClickListener(mListener);

        findViewById(R.id.main_button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });
    }

    View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            AppDownLoadHelper helper = AppDownLoadManager.getInstance().getHelperByTag("xx");
            if(helper != null){
                AppDownLoadManager.getInstance().cancelHelperByTag("xx");
                mDownButton.setText("下载");
                return;
            }

            new AppDownLoadHelper.Builder()
                    .setPath(SDCardUtil.getLogCacheDir(AppApplication.getContext()) + "/xxx1.apk")
                    .setTag("xx")
                    .setUrl("http://dl.play.91.com/bigdata/com.ilongyuan.voez.baidu_v1.0.4.apk")
                    .setDownLoadListener(new AppProgressListener() {
                        @Override
                        public void onStart() {
                            Log.i("tag", "========开始");
                            mDownButton.setText("0%");
                        }

                        @Override
                        public void update(long bytesRead, long contentLength, boolean done) {
                            int read = (int)(bytesRead * 100f / contentLength);
                            Log.i("tag", "========" + read);
                            mDownButton.setText(read + "%");
                        }

                        @Override
                        public void onCompleted() {
                            mDownButton.setText("完成");
                            Log.i("tag", "========" + Thread.currentThread().getName());
                        }

                        @Override
                        public void onError(String err) {
                            mDownButton.setText("失败");
                            Log.i("tag", "========失败" + err);
                        }
                    })
                    .create()
                    .execute();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppDownLoadManager.getInstance().unsubscribe();
    }
}
