package independent_study.lightningtimer;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Blaine Huey
 */

public class MainActivity extends AppCompatActivity
{
    private TextView timeTextView;
    private Button startStopButton;
    private Handler handler;
    private Runnable runnable;

    private boolean buttonState;
    private long timerCount;
    private long startCount;
    private long lastCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Lightning Timer", "Main Activity onCreate()");

        timeTextView = (TextView) findViewById(R.id.TimerTextView);
        startStopButton = (Button) findViewById(R.id.StartStopButton);
        handler = new Handler();

        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                lastCount = System.currentTimeMillis();
                timerCount = lastCount - startCount;
                timeTextView.setText(Long.toString(timerCount));
                handler.postDelayed(this, 0);
            }
        };

        buttonState = false;
        timerCount = 0L;
        startCount = System.currentTimeMillis();
        lastCount = startCount;

        //https://stackoverflow.com/questions/8977212/button-click-listeners-in-android
        startStopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!buttonState)
                {
                    buttonState = true;
                    startStopButton.setText("Stop Timer");
                    activateTimer();
                }
                else
                {
                    buttonState = false;
                    startStopButton.setText("Start Timer");
                    deactivateTimer();
                }
            }
        });
    }

    private void activateTimer()
    {
        Log.d("Lightning Timer", "Button Activated");

        timerCount = 0;
        startCount = System.currentTimeMillis();

        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    private void deactivateTimer()
    {
        Log.d("Lightning Timer", "Button De-Activated");
        handler.removeCallbacks(runnable);
    }
}
