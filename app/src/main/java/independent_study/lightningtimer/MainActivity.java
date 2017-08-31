package independent_study.lightningtimer;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    private TextView timeTextView;
    private Button startStopButton;
    private Handler handler;

    private boolean buttonState;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Lightning Timer", "Main Activity onCreate()");

        timeTextView = (TextView) findViewById(R.id.TimerTextView);
        startStopButton = (Button) findViewById(R.id.StartStopButton);
        handler = new Handler();

        buttonState = false;

        startStopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!buttonState)
                {
                    buttonState = true;
                    startStopButton.setText("Stop");
                    activateTimer();
                }
                else
                {
                    buttonState = false;
                    startStopButton.setText("Start");
                    deactivateTimer();
                }
            }
        });
    }

    private void activateTimer()
    {

    }

    private void deactivateTimer()
    {
        
    }
}
