package si.urban.invapoint;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class LaunchScreen extends AppCompatActivity {
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        getWindow().getDecorView().setBackgroundColor(Color.WHITE);


        mHandler.postDelayed(new Runnable() {
            public void run() {
                doStuff();
            }
        }, 1000);
    }

    private void doStuff() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
