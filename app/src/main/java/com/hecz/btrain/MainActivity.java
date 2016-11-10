package com.hecz.btrain;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ExerciseController exerciseController;
    private Button button;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private RelativeLayout relativeLayoutMenu;
    private RelativeLayout relativeLayoutScreen;

    private SharedPreferences prefs;
    private Editor editor;

    private TextView textViewBreathVelocity;
    private Button buttonLess;
    private Button buttonMore;

    private static String profile;

    private AppCompatActivity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        this.activity = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        editor = prefs.edit();

        textViewBreathVelocity = (TextView) findViewById(R.id.textViewBreathVelocity);
        exerciseController = new ExerciseController(this, (TextView) findViewById(R.id.textViewBreathPhase));
        textViewBreathVelocity.setText(String.valueOf(exerciseController.getBreathingLevel()));

        buttonLess = (Button) findViewById(R.id.button_less);
        buttonLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int breathingLevel = exerciseController.getBreathingLevel();
                if(breathingLevel > 5) {
                    breathingLevel--;
                    exerciseController.setBreathingLevel(breathingLevel);
                    textViewBreathVelocity.setText(String.valueOf(breathingLevel));
                    editor.putInt(profile,breathingLevel);
                    editor.commit();
                }
                else{
                    Toast.makeText(MainActivity.this, R.string.cannot_less, Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonMore = (Button) findViewById(R.id.button_more);
        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int breathingLevel = exerciseController.getBreathingLevel();
                if(breathingLevel < 14) {
                    breathingLevel++;
                    exerciseController.setBreathingLevel(breathingLevel);
                    textViewBreathVelocity.setText(String.valueOf(breathingLevel));
                    editor.putInt(profile,breathingLevel);
                    editor.commit();
                }
                else{
                    Toast.makeText(MainActivity.this, R.string.cannot_more, Toast.LENGTH_SHORT).show();
                }
            }
        });

        relativeLayoutMenu = (RelativeLayout) findViewById(R.id.activity_main_menu);
        relativeLayoutScreen = (RelativeLayout) findViewById(R.id.activity_main_screen);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int breathingLevel = prefs.getInt(String.valueOf(R.string.text_button_menu1),5);
                relativeLayoutMenu.setVisibility(View.GONE);
                relativeLayoutScreen.setVisibility(View.VISIBLE);
                exerciseController.setBreathingLevel(breathingLevel);
                textViewBreathVelocity.setText(String.valueOf(breathingLevel));
                profile = new String(String.valueOf(R.string.text_button_menu1));
            }
        });
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int breathingLevel = prefs.getInt(String.valueOf(R.string.text_button_menu2),10);
                relativeLayoutMenu.setVisibility(View.GONE);
                relativeLayoutScreen.setVisibility(View.VISIBLE);
                exerciseController.setBreathingLevel(breathingLevel);
                textViewBreathVelocity.setText(String.valueOf(breathingLevel));
                profile = new String(String.valueOf(R.string.text_button_menu2));
            }
        });
        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int breathingLevel = prefs.getInt(String.valueOf(R.string.text_button_menu3),14);
                relativeLayoutMenu.setVisibility(View.GONE);
                relativeLayoutScreen.setVisibility(View.VISIBLE);
                exerciseController.setBreathingLevel(breathingLevel);
                textViewBreathVelocity.setText(String.valueOf(breathingLevel));
                profile = new String(String.valueOf(R.string.text_button_menu3));
            }
        });
        button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                TextView message = new TextView(activity);
                message.setText(R.string.text_button_menu4);
                message.setGravity(Gravity.CENTER_HORIZONTAL);
                message.setTextSize(20);
                message.setTextColor(Color.BLUE);
                builder.setCustomTitle(message);
                builder.setMessage(R.string.text_about_application);
                builder.setPositiveButton(R.string.ok, null);
                AlertDialog dialog = builder.show();
            }
        });
        button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.super.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(relativeLayoutMenu.getVisibility() == View.VISIBLE){
            MainActivity.super.finish();
        }
        else {
            relativeLayoutScreen.setVisibility(View.INVISIBLE);
            relativeLayoutMenu.setVisibility(View.VISIBLE);
        }
    }
}
