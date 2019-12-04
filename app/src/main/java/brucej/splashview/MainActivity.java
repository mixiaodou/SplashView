package brucej.splashview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private FrameLayout splashLayout;
    private SplashView splashView;
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                showExample();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        splashLayout = findViewById(R.id.splash_layout);
        splashView = new SplashView(this);
        splashLayout.addView(splashView,
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT)
        );
        showExample();
    }
    //
    private void showExample() {
        splashView.postDelayed(new Runnable() {
            @Override
            public void run() {
                splashView.finishLoading();
                handler.sendEmptyMessageDelayed(0, 5000);
            }
        }, 2000);
        splashView.startLoading();
    }
}
