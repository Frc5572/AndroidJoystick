package com.example.projectowen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import net.ffst.adbpotato.Bridge;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static class InnerThread extends Thread {

        private boolean running;
        private final MainActivity activity;
        private JoystickApp app;

        private static final int buttonIds[] = new int[]{
                R.id.l0c0, R.id.l1c0, R.id.l2c0,
                R.id.l0c1, R.id.l1c1, R.id.l2c1,
                R.id.l0c2, R.id.l1c2, R.id.l2c2,
                R.id.l0c3, R.id.l1c3, R.id.l2c3,
                R.id.l0c4, R.id.l1c4, R.id.l2c4,
                R.id.l0c5, R.id.l1c5, R.id.l2c5,
                R.id.l0c6, R.id.l1c6, R.id.l2c6,
                R.id.l0c7, R.id.l1c7, R.id.l2c7,
                R.id.l0c8, R.id.l1c8, R.id.l2c8,
        };

        public InnerThread(MainActivity activity, JoystickApp app) {
            this.activity = activity;
            this.app = app;
        }

        @Override
        public void run() {
            running = true;
            while (running) {
                activity.runOnUiThread(() -> {
                    for (int col = 0; col < 9; col++) {
                        for (int level = 0; level < 3; level++) {
                            activity.findViewById(buttonIds[col * 3 + level]).setBackgroundColor(app.getGridState(level, col).backgroundColor);
                        }
                    }
                });
                try {
                    sleep(20); // TODO figure out if we need this and for how long it needs to be
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void end() {
            running = false;
        }
    }

    private InnerThread thread;

    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        JoystickApp app = ((JoystickApp) getApplication());
        thread = new InnerThread(this, app);
        // cone led control
        findViewById(R.id.ledCone).setOnClickListener((btn) -> {
            try {
                app.bridge.publish("LED Cone", true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // cube led control
        findViewById(R.id.ledCube).setOnClickListener((btn) -> {
            try {
                app.bridge.publish("LED Cube", true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        findViewById(R.id.home).setOnClickListener((btn) -> {
            try {
                app.bridge.publish("Home", true);
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        });
        ((SeekBar)findViewById(R.id.rollers)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try {
                    app.bridge.publish("slider", seekBar.getProgress() / (double) seekBar.getMax() * 2.0 - 1.0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(seekBar.getMax() / 2);
                try {
                    app.bridge.publish("slider", 0.0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            thread.end();
            thread.join();
        } catch (InterruptedException ignored) {
        }
        super.onDestroy();
    }

    public void gotoAuto(View v) {
        startActivity(new Intent(MainActivity.this, AutoActivity.class));
    }
}