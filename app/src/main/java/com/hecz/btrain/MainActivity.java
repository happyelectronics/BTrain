package com.hecz.btrain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    ExerciseController exerciseController;
    TextView textViewBreathVelocity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewBreathVelocity = (TextView) findViewById(R.id.textViewBreathVelocity);
        exerciseController = new ExerciseController(this,(TextView) findViewById(R.id.textViewBreathPhase));
        textViewBreathVelocity.setText(String.valueOf(exerciseController.getBreathingDuration()));
    }
}
