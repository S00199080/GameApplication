package com.example.myapplicationtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final int BLUE = 1;
    private final int RED = 2;
    private final int YELLOW = 3;
    private final int GREEN = 4;

    Button bRed, bBlue, bYellow, bGreen, fb;
    int sequenceCount = 4, n = 0;
    private Object mutex = new Object();
    int[] gameSequence = new int[120];
    int arrayIndex = 0;

    // experimental values for hi and lo magnitude limits
    private final double NORTH_MOVE_FORWARD = 6.0;     // upper mag limit
    private final double NORTH_MOVE_BACKWARD = 3.0;      // lower mag limit
    boolean highLimit = false;      // detect high limit
    int counter = 0;                // step counter

    TextView tvx, tvy, tvz, tvSteps;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    CountDownTimer ct = new CountDownTimer(6000,  1500) {

        public void onTick(long millisUntilFinished) {
            //mTextField.setText("seconds remaining: " + millisUntilFinished / 1500);
            oneButton();
            //here you can have your logic to set text to edittext
        }

        public void onFinish() {
            //mTextField.setText("done!");
            // we now have the game sequence

            for (int i = 0; i< arrayIndex; i++)
                Log.d("game sequence", String.valueOf(gameSequence[i]));
            // start next activity

            // put the sequence into the next activity
            // stack overglow https://stackoverflow.com/questions/3848148/sending-arrays-with-intent-putextra
            //Intent i = new Intent(A.this, B.class);
            //i.putExtra("numbers", array);
            //startActivity(i);

            // start the next activity
            // int[] arrayB = extras.getIntArray("numbers");
        }
    };
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bRed = findViewById(R.id.btnRed);
        bBlue = findViewById(R.id.btnBlue);
        bYellow = findViewById(R.id.btnYellow);
        bGreen = findViewById(R.id.btnGreen);


        tvx = findViewById(R.id.tvX);
        tvy = findViewById(R.id.tvY);
        tvz = findViewById(R.id.tvZ);
        tvSteps = findViewById(R.id.tvSteps);

        // we are going to use the sensor service
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void doPlay(View view) {
        ct.start();
    }

    protected void onResume() {
        super.onResume();
        // turn on the sensor
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);    // turn off listener to save power
    }
    @Override

    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];



        tvx.setText(String.valueOf(x));
        tvy.setText(String.valueOf(y));
        tvz.setText(String.valueOf(z));


        // Can we get a north movement

        // you need to do your own mag calculating
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            if (y > NORTH_MOVE_FORWARD) {
                // tilt forward
                bRed.performClick();
            } else if (y < NORTH_MOVE_BACKWARD) {
                // tilt backward
                bYellow.performClick();
            } else if (x < 0) {
                // tilt left
                bBlue.performClick();
            } else if (x > 0) {
                // tilt right
                bGreen.performClick();
            }
        }
        }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not used
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    private void oneButton() {
        n = getRandom(sequenceCount);

        Toast.makeText(this, "Number = " + n, Toast.LENGTH_SHORT).show();

        switch (n) {
            case 1:
                flashButton(bBlue);
                gameSequence[arrayIndex++] = BLUE;
                break;
            case 2:
                flashButton(bRed);
                gameSequence[arrayIndex++] = RED;
                break;
            case 3:
                flashButton(bYellow);
                gameSequence[arrayIndex++] = YELLOW;
                break;
            case 4:
                flashButton(bGreen);
                gameSequence[arrayIndex++] = GREEN;
                break;
            default:
                break;
        }   // end switch
    }

    //
    // return a number between 1 and maxValue
    private int getRandom(int maxValue) {
        return ((int) ((Math.random() * maxValue) + 1));
    }

    private void flashButton(Button button) {
        fb = button;
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {

                fb.setPressed(true);
                fb.invalidate();
                fb.performClick();
                Handler handler1 = new Handler();
                Runnable r1 = new Runnable() {
                    public void run() {
                        fb.setPressed(false);
                        fb.invalidate();
                    }
                };
                handler1.postDelayed(r1, 600);

            } // end runnable
        };
        handler.postDelayed(r, 600);
    }
    

    public void doTest(View view) {
        for (int i = 0; i < sequenceCount; i++) {
            int x = getRandom(sequenceCount);

            Toast.makeText(this, "Number = " + x, Toast.LENGTH_SHORT).show();

            if (x == 1)
                flashButton(bBlue);
            else if (x == 2)
                flashButton(bRed);
            else if (x == 3)
                flashButton(bYellow);
            else if (x == 4)
                flashButton(bGreen);
        }
        bRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if this button is the next in the sequence
                if (gameSequence[n] == RED) {
                    // correct button
                    n++;
                    if (n == sequenceCount) {
                        // end of sequence, call leaderBoards method
                        leaderBoards();
                    }
                } else {
                    // incorrect button, reset the sequence
                    n = 0;
                }
            }
        });

        bYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if this button is the next in the sequence
                if (gameSequence[n] == YELLOW) {
                    // correct button
                    n++;
                    if (n == sequenceCount) {
                        // end of sequence, call leaderBoards method
                        leaderBoards();
                    }
                } else {
                    // incorrect button, reset the sequence
                    n = 0;
                }
            }
        });

        bBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if this button is the next in the sequence
                if (gameSequence[n] == BLUE) {
                    // correct button
                    n++;
                    if (n == sequenceCount) {
                        // end of sequence, call leaderBoards method
                        leaderBoards();
                    }
                } else {
                    // incorrect button, reset the sequence
                    n = 0;
                }
            }
        });

        bGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if this button is the next in the sequence
                if (gameSequence[n] == GREEN) {
                    // correct button
                    n++;
                    if (n == sequenceCount) {
                        // end of sequence, call leaderBoards method
                        leaderBoards();
                    }
                } else {
                    // incorrect button, reset the sequence
                    n = 0;
                }
            }
        });
    }

    private void leaderBoards() {





        DatabaseHandler db = new DatabaseHandler(this);

        db.emptyHiScores();     // empty table if required

        // Inserting hi scores
        Log.i("Insert: ", "Inserting ..");
        db.addHiScore(new HiScore("20 OCT 2020", "Test1", 12));
        db.addHiScore(new HiScore("28 OCT 2020", "Test2", 16));
        db.addHiScore(new HiScore("20 NOV 2020", "Test3", 20));
        db.addHiScore(new HiScore("20 NOV 2020", "Test4", 18));
        db.addHiScore(new HiScore("22 NOV 2020", "Test5", 22));
        db.addHiScore(new HiScore("30 NOV 2020", "Test6", 30));
        db.addHiScore(new HiScore("01 DEC 2020", "Test7", 22));
        db.addHiScore(new HiScore("02 DEC 2020", "Test8", 132));


        // Reading all scores
        Log.i("Reading: ", "Reading all scores..");
        List<HiScore> hiScores = db.getAllHiScores();


        for (HiScore hs : hiScores) {
            String log =
                    "Id: " + hs.getScore_id() +
                            ", Date: " + hs.getGame_date() +
                            " , Player: " + hs.getPlayer_name() +
                            " , Score: " + hs.getScore();

            // Writing HiScore to log
            Log.i("Score: ", log);
        }

        Log.i("divider", "====================");

        HiScore singleScore = db.getHiScore(5);
        Log.i("High Score 5 is by ", singleScore.getPlayer_name() + " with a score of " +
                singleScore.getScore());

        Log.i("divider", "====================");

        // Calling SQL statement
        List<HiScore> top5HiScores = db.getTopFiveScores();

        for (HiScore hs : top5HiScores) {
            String log =
                    "Id: " + hs.getScore_id() +
                            ", Date: " + hs.getGame_date() +
                            " , Player: " + hs.getPlayer_name() +
                            " , Score: " + hs.getScore();

            // Writing HiScore to log
            Log.i("Score: ", log);
        }
        Log.i("divider", "====================");

        HiScore hiScore = top5HiScores.get(top5HiScores.size() - 1);
        // hiScore contains the 5th highest score
        Log.i("fifth Highest score: ", String.valueOf(hiScore.getScore()) );

        // simple test to add a hi score
        int myCurrentScore = 40;
        // if 5th highest score < myCurrentScore, then insert new score
        if (hiScore.getScore() < myCurrentScore) {
            db.addHiScore(new HiScore("08 DEC 2020", "Elrond", 40));
        }

        Log.i("divider", "====================");

        // Calling SQL statement
        top5HiScores = db.getTopFiveScores();
        List<String> scoresStr;
        scoresStr = new ArrayList<>();

        int j = 1;
        for (HiScore hs : top5HiScores) {

            String log =
                    "Id: " + hs.getScore_id() +
                            ", Date: " + hs.getGame_date() +
                            " , Player: " + hs.getPlayer_name() +
                            " , Score: " + hs.getScore();

            // store score in string array
            scoresStr.add(j++ + " : "  +
                    hs.getPlayer_name() + "\t" +
                    hs.getScore());
            // Writing HiScore to log
            Log.i("Score: ", log);
        }

        Log.i("divider", "====================");
        Log.i("divider", "Scores in list <>>");
        for (String ss : scoresStr) {
            Log.i("Score: ", ss);
        }

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scoresStr);
        listView.setAdapter(itemsAdapter);

    }
    }
