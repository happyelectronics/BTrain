package com.hecz.btrain;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Vector;

public class ExerciseController {

    // time/min;inhale;hold;exhale;hold
    String[] bhold = {
            "5;3600;600;7200;600",
            "6;3000;500;6000;500",
            "7;2500;400;5100;400",
            "8;2200;300;4500;300",
            "9;1900;300;4000;300",
            "10;1800;300;3600;300",
            "11;1600;200;3200;200",
            "12;1500;200;3000;200",
            "13;1300;200;2800;200",
            "14;1200;200;2500;200"
    };

    private static final int BREATH_INHALATE = 0;
    private static final int BREATH_HOLD = 1;
    private static final int BREATH_EXHALATE = 2;

    private int breathPhase;

    private int breathingLevel = 5;

    protected TextView textViewBreathPhase;

	private static final int BREATH_REGULAR = 0;
	private static final int BREATH_HOLDING = 1;
	protected static ExerciseController instance;
	private boolean isExerciseOnline = false;
	private ImageView imageCoach;
	private ImageButton buttonSound;
	//private SeekBar seekBar;
	private TextView textBreathing;
	public final Activity activity;
	private SharedPreferences prefs;
	private Editor editor;
	private boolean exit = false;
	private boolean exeStop = false;
	private Vector<MediaPlayer> mp = new Vector<MediaPlayer>(3);
	private int soundVolume;
	private int volumeMax;
	private int currentVolume;
	private ScaleAnimation scalerExhale;
	private ScaleAnimation scalerInhale;
    private ScaleAnimation scalerHold;

    private int state = 0;

	protected boolean isNewDuration = false;

	public int breathingType = BREATH_REGULAR;
	private int duration = 5000;
	private long lastTime = 0;
    private int currentBreathing = 5000;
    private int breathingDuration = 5000;


    public ExerciseController(Activity _activity, final TextView textViewBreathPhase) {
        this.textViewBreathPhase = textViewBreathPhase;
        Log.i(Global.APP_LOG_PREFIX + "ZEN", "ExerciseController");
        instance = this;
        this.activity = _activity;

        PreferenceManager.setDefaultValues(activity, R.xml.preferences, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);


        //TODO predelat, tady se vybiralo ze skupiny dechu
        int iBreathingPos = 0;
        if (iBreathingPos < 1) {
            breathingType = BREATH_REGULAR;
        } else {
            breathingType = BREATH_HOLDING;
        }

        editor = prefs.edit();

        mp.add(0, new MediaPlayer());
        mp.add(1, new MediaPlayer());
        mp.add(2, new MediaPlayer());

        final AudioManager audio = (AudioManager) activity
                .getSystemService(Context.AUDIO_SERVICE);
        if (audio != null) {
            volumeMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

            soundVolume = (4 * currentVolume) / volumeMax;

            if (soundVolume > 3) {
                soundVolume = 3;
            }
        }
        /*
		 * audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
		 * AudioManager.ADJUST_RAISE, 0);
		 * audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
		 * AudioManager.ADJUST_LOWER, 0);
		 */
        // audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
        // AudioManager audio = (AudioManager)
        // getSystemService(Context.AUDIO_SERVICE);

        AssetFileDescriptor afd;
        try {
            for (int i = 0; i < 3; i++) {
                afd = activity.getAssets().openFd(
                        "metronom" + Integer.toString(i) + ".mp3");
                Log.d(Global.APP_LOG_PREFIX + "FILE",
                        "metronom" + afd.getFileDescriptor().valid() + " - "
                                + Integer.toString(i) + ".mp3");
                if (afd.getFileDescriptor().valid()) {
                    mp.get(i).setDataSource(afd.getFileDescriptor(),
                            afd.getStartOffset(), afd.getLength());
                    mp.get(i).prepare();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageCoach = (AnimationImageView) activity.findViewById(R.id.obrazek);

		/*buttonSound = (ImageButton) activity.findViewById(R.id.proImageButton1);

		if (buttonSound != null) {
			if (audio != null && buttonSound != null) {
				buttonSound.setVisibility(View.VISIBLE);
			}

			showVolume();

			buttonSound.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!Global.METRONOM_ENABLED) {
						Tools.showCustomDialog(R.string.metronom, 0, activity);
					} else {

						soundVolume++;
						if (soundVolume > 3) {
							soundVolume = 0;
						}
						showVolume();
						int newVolume = (volumeMax * soundVolume) / 3;
						if (newVolume > volumeMax) {
							newVolume = volumeMax;
						}
						if (audio != null) {
                            Log.i(Global.APP_LOG_PREFIX + "ZEN", "ExerciseController");
							audio.setStreamVolume(AudioManager.STREAM_MUSIC,
									newVolume, 0);
						}
					}
				}

			});
		}
		*/

        // imageCoach = (ImageView) activity.findViewById(R.id.obrazek);

        //nastavovani aktualni rychlosti
        //seekBar = (SeekBar) activity.findViewById(R.id.seekBar1);
        //textBreathing = (TextView) activity.findViewById(R.id.textViewTime);

        //duration = prefs.getString("duration", "3100");
        //setDuration(Integer.parseInt(prefs.getString("duration", "3000")));

        scalerExhale = new ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        durationExhale = getDuration();
        if (Global.isZen) {
            durationExhale = (int) (Global.timeMaxLf / 2);
        }
        hold1 = 0;
        hold2 = 0;

        // scaler.setStartOffset(hold1);
        // scaler.setDuration(duration2);

        // scaler.setStartTime(duration2 / 2);

        // scaler.setRepeatCount(Animation.INFINITE);
        scalerExhale.setRepeatCount(0);
        scalerExhale.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //textViewBreathPhase.setText(R.string.text_exhalate);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageCoach.setScaleX(0.1f);
                imageCoach.setScaleY(0.1f);
                textViewBreathPhase.setText(R.string.text_hold);
                scalerHold.setDuration(durationHold);
                imageCoach.startAnimation(scalerHold);
                state++;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // scaler.setRepeatMode(Animation.REVERSE);
        scalerInhale = new ScaleAnimation(1.0f, 10.0f, 1.0f, 10.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);

        scalerInhale.setRepeatCount(0);
        scalerInhale.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageCoach.setScaleX(1.0f);
                imageCoach.setScaleY(1.0f);
                textViewBreathPhase.setText(R.string.text_hold);
                scalerHold.setDuration(durationHold);
                imageCoach.startAnimation(scalerHold);
                state++;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        durationInhale = getDuration();
        Log.i(Global.APP_LOG_PREFIX + "ZEN", "isZen = " + Global.isZen
                + "timeMaxHf = " + Global.timeMaxHf + ", timeMaxLf = "
                + Global.timeMaxLf);
        if (Global.isZen) {
            durationInhale = (int) (Global.timeMaxHf / 2);
        }

        // scaler3.setDuration(duration3);
        // scaler3.setStartOffset(duration2+hold1+hold2);

        scalerHold = new ScaleAnimation(1.0f, 1.0f, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);

        scalerHold.setRepeatCount(0);
        scalerHold.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(state == 1) {
                    textViewBreathPhase.setText(R.string.text_inhalate);
                    scalerInhale.setDuration(durationInhale);
                    imageCoach.startAnimation(scalerInhale);
                    state++;
                }
                if(state == 3){
                    textViewBreathPhase.setText(R.string.text_exhalate);
                    scalerExhale.setDuration(durationExhale);
                    imageCoach.startAnimation(scalerExhale);
                    state=0;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        durationHold = getDuration();
        if (Global.isZen) {
            durationHold = (int) (Global.timeMaxLf / 2);
        }
        hold1 = 0;
        hold2 = 0;

        // scaler.setStartOffset(hold1);
        // scaler.setDuration(duration2);

        // scaler.setStartTime(duration2 / 2);

        // scaler.setRepeatCount(Animation.INFINITE);

        setScalers();

        // scaler3.setRepeatCount(Animation.INFINITE);

        // scaler3.setRepeatMode(Animation.REVERSE);
        state = 0;
        textViewBreathPhase.setText(R.string.text_exhalate);
        scalerExhale.setDuration(durationExhale);
        imageCoach.startAnimation(scalerExhale);


        if (imageCoach != null) {
            Log.i(Global.APP_LOG_PREFIX + "ZEN", "III - imageCoach = " + imageCoach.getWidth());
            //restartAnimation();
        } else {
            //Log.i(Global.APP_LOG_PREFIX + "ZEN", "III - return "+imageCoach.getWidth());
            return;
        }

        //setExeStop(true);

        if (breathingType == BREATH_REGULAR) {
            //seekBar.setProgress((durationExhale - 2980) / 45);
        } else {
            int time = getCurrentBreathing();
            //seekBar.setProgress((44000 - time * 2980) / (45 * time));
        }

        if (textBreathing != null) {
            if (breathingType == BREATH_REGULAR) {
                textBreathing.setText(String.format(" %.1f sec",  ((float) durationExhale) / 500));
            } else {

                textBreathing.setText(String
                        .format(" %d/min", getCurrentBreathing() + 4));
            }
        }

        Log.i(Global.APP_LOG_PREFIX + "SEEK", "INIT SEEKBAR");
        /*seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // scaler.setDuration(35*progress + 1465);
                if (!Global.isZen) {
                    int tmpDuration = 45 * progress + 2980;
                    //duration = Integer.toString(tmpDuration);

                    isNewDuration = true;

                    Log.i(Global.APP_LOG_PREFIX + "SEEK", "onProgressChanged = " + tmpDuration);

                    //setDuration(tmpDuration);

                    if (textBreathing != null) {
                        if (breathingType == BREATH_REGULAR) {
                            textBreathing.setText(String.format(" %.1f sec",
                                    ((float) tmpDuration) / 500));
                        } else {
                            int time = (int) 44000 / tmpDuration;
                            setCurrentBreathing(time - 4);
                            textBreathing.setText(String
                                    .format(" %d/min", time));
                        }
                    }

                    setBreathingDuration(tmpDuration);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }
        });
*/
        if (Global.METRONOM_ENABLED) {
            Log.i(Global.APP_LOG_PREFIX + "ZEN", "New Thread");
            soundThread.start();
        }
    }

    private void setScalers() {
        /*setExeStop(true);
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		setExeStop(false);
*/      durationHold = 1000;
        if (breathingType == BREATH_REGULAR) {
            int parseInt = getBreathingDuration();
            durationExhale = parseInt;
            durationInhale = parseInt;
            hold1 = 0;
            hold2 = 0;
        } else if (breathingType == BREATH_HOLDING) {
            int iSelect = getCurrentBreathing();
            durationExhale = getExhale(iSelect);
            durationInhale = getInhale(iSelect);
            hold1 = getHold2(iSelect);
            ;
            hold2 = getHold1(iSelect);
            ;
        }
        scalerExhale.setDuration(durationExhale);

        Log.i(Global.APP_LOG_PREFIX + "ZEN", "isZen = " + Global.isZen
                + "durationExhale = " + durationExhale + ", durationInhale = "
                + durationInhale + ", hold2 = " + hold2 + ", hold1 = " + hold1);
        if (Global.isZen) {
            durationInhale = (int) (Global.timeMaxHf / 2);
        }

        scalerInhale.setDuration(durationInhale);
        scalerHold.setDuration(durationHold);
    }

    private int getHold1(int iSelect) {
        return 0;
    }

    private int getHold2(int iSelect) {
        return 1000;
    }

    private int getInhale(int iSelect) {
        return 1100;
    }

    private int getExhale(int iSelect) {
        return 1000;
    }


	public boolean isExerciseOnline() {
		return isExerciseOnline;
	}

	public void setExerciseOnline(boolean isExerciseOnline) {
		this.isExerciseOnline = isExerciseOnline;
		Log.d(Global.APP_LOG_PREFIX + "Exercise", "" + isExerciseOnline);
	}

	public int getDuration() {
		return durationInhale+durationExhale+(2*durationHold);
	}

	//public void setDuration(int duration) {
	//	this.duration = duration;
	//}

	public boolean isExit() {
		return exit;
	}

	public void setExit() {
		this.exit = true;
	}

	public boolean isExeStop() {
		return exeStop;
	}

	public void setExeStop(boolean exeStop) {
        Log.i(Global.APP_LOG_PREFIX + "ZEN", "setExeStop: "+exeStop);
		this.exeStop = exeStop;
		if (exeStop) {
			//as.cancel();
		} else {
			AnimationHandler.sendEmptyMessage(0);
		}
	}

	static Handler AnimationHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.i(Global.APP_LOG_PREFIX + "ZEN", "handleMessage");

			ExerciseController.getInstance().restartAnimation2();
		}
	};

	private Thread soundThread = new Thread(new Runnable() {

		@Override
		public void run() {
			try {

				int lastDuration = getDuration();
				int sampleMP3 = 0;
                Log.d(Global.APP_LOG_PREFIX + "-Thread1","EC - soundThread");

				while (!isExit()) {
                    //Log.d(Global.APP_LOG_PREFIX + "-Thread1","soundThread !isExit");
					if (isExerciseOnline() && !isExeStop()) {

                        //Log.d(Global.APP_LOG_PREFIX + "-Thread1","soundThread isExerciseOnline() && !isExeStop()");
						/*if (infoPanel != null) {

							int inf = Math.max(infoPanel.getConcentration(),
									infoPanel.getRelaxation());

							//Log.d(Global.APP_LOG_PREFIX + "-Thread1","soundThread max conc:"+inf);
							if (inf < 20) {
                                sampleMP3 = 0;
							}
							else if (inf < 50) {
								sampleMP3 = 1;
							}
							else if (inf >= 50) {
								sampleMP3 = 2;
							}
						}*/

						final int isampleMP3 = sampleMP3;
						new Thread(new Runnable() {
							@Override
							public void run() {
								//Log.d(Global.APP_LOG_PREFIX + "-Thread1", "(1) soundThread: "+isampleMP3);
								mp.get(isampleMP3).start();
								// Log.d(Global.APP_LOG_PREFIX + "-Thread1",
								// "(2)");
							}
						}).start();

						Thread.sleep(getDuration());

						

						if (mp.get(sampleMP3).isPlaying()) {
                            //Log.i(Global.APP_LOG_PREFIX + "ZEN", "ExerciseControlle soundThread:r"+sampleMP3);
                            mp.get(sampleMP3).pause();
                        }
						mp.get(sampleMP3).seekTo(0);
					} else {
						Thread.sleep(5);
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	});
	private AnimationSet as;
	private int hold1 = 0;
	private int hold2 = 20;
	private int durationInhale = 0;
	private int durationExhale = 0;
    private int durationHold = 0;

	protected void restartAnimation() {
		if (isNewDuration) {
			isNewDuration = false;
			setScalers();
		}
		if (imageCoach != null) {
			//as.reset();

			//as.start();
			Log.i(Global.APP_LOG_PREFIX + "ZEN", "Restart - imageCoach = " + imageCoach.getWidth());
		}
	}

	protected void restartAnimation2() {
		if (isNewDuration) {
			isNewDuration = false;
			setScalers();
		}
		if (imageCoach != null) {
			imageCoach.clearAnimation();
			imageCoach.startAnimation(as);
			//as.start();
			Log.i(Global.APP_LOG_PREFIX + "ZEN", "Restart - imageCoach = " + imageCoach.getWidth());
		}
	}

	protected static ExerciseController getInstance() {
		return instance;
	}

    public int getCurrentBreathing() {
        return currentBreathing;
    }

    public void setCurrentBreathing(int currentBreathing) {
        this.currentBreathing = currentBreathing;
    }

    public void setBreathingDuration(int breathingDuration) {
        this.breathingDuration = breathingDuration;
    }

    public int getBreathPhase(){
        return breathPhase;
    }

    public int getBreathingDuration() {
        return breathingDuration;
    }

    public int getBreathingLevel(){
        return breathingLevel;
    }

    public void setBreathingLevel(int number){
        if(number >= 5 && number <= 14) breathingLevel = number;
        switch(number){
            case 5:
                durationInhale = 3600;
                durationExhale = 7200;
                durationHold = 600;
                break;
            case 6:
                durationInhale = 3000;
                durationExhale = 6000;
                durationHold = 600;
                break;
            case 7:
                durationInhale = 2500;
                durationExhale = 5100;
                durationHold = 400;
                break;
            case 8:
                durationInhale = 2200;
                durationExhale = 4500;
                durationHold = 300;
                break;
            case 9:
                durationInhale = 1900;
                durationExhale = 4000;
                durationHold = 300;
                break;
            case 10:
                durationInhale = 1800;
                durationExhale = 3600;
                durationHold = 300;
                break;
            case 11:
                durationInhale = 1600;
                durationExhale = 3200;
                durationHold = 200;
                break;
            case 12:
                durationInhale = 1500;
                durationExhale = 3000;
                durationHold = 200;
                break;
            case 13:
                durationInhale = 1300;
                durationExhale = 2800;
                durationHold = 200;
                break;
            case 14:
                durationInhale = 1200;
                durationExhale = 2500;
                durationHold = 200;
                break;
        }
    }
}
