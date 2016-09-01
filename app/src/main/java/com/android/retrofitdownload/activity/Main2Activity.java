package com.android.retrofitdownload.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import com.android.retrofitdownload.R;
import com.android.retrofitdownload.download.AppDownLoadHelper;
import com.android.retrofitdownload.download.AppDownLoadManager;
import com.android.retrofitdownload.download.AppProgressListener;

public class Main2Activity extends AppCompatActivity {

    private AppDownLoadHelper mHelper;
    private Button mDownButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mDownButton = (Button) findViewById(R.id.main2_button_down);

        mHelper = AppDownLoadManager.getInstance().getHelperByTag("xx");
        if(mHelper != null) {
            mHelper.registerListener(mProgressLinstener);
        }
    }

    AppProgressListener mProgressLinstener = new AppProgressListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void update(long bytesRead, long contentLength, boolean done) {
            int read = (int)(bytesRead * 100f / contentLength);
            mDownButton.setText(read + "%");
        }

        @Override
        public void onCompleted() {
            mDownButton.setText("完成");
        }

        @Override
        public void onError(String err) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mHelper != null) {
            mHelper.unRegisterListener(mProgressLinstener);
        }
    }
}
