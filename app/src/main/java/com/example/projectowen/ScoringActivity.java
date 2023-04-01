package com.example.projectowen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScoringActivity extends AppCompatActivity {

    private static class InnerThread extends Thread {

        private boolean running;
        private final ScoringActivity activity;
        private JoystickApp app;

        public static final int buttonIds[] = new int[]{
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

        public InnerThread(ScoringActivity activity, JoystickApp app) {
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


    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_score);
        JoystickApp app = ((JoystickApp) getApplication());
        thread = new InnerThread(this, app);
        thread.start();
        AtomicBoolean gridStatus = new AtomicBoolean(false);
        // cone led control
        this.findViewById(R.id.ledCone).setOnClickListener((btn) -> {
            try {
                app.bridge.publish("LED Cone", 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // cube led control
        this.findViewById(R.id.ledCube).setOnClickListener((btn) -> {
            try {
                app.bridge.publish("LED Cube", 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        this.findViewById(R.id.home).setOnClickListener((btn) -> {
            try {
                app.bridge.publish("Home", 1);
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        });
        this.findViewById(R.id.autoTab).setOnClickListener((btn) -> {
            startActivity(new Intent(ScoringActivity.this, AutoActivity.class));
            finish();
        });
        this.findViewById(R.id.gridmark).setOnClickListener((btn) -> {
            gridStatus.set(!gridStatus.get());
        });
        for (int col = 0; col < 9; col++) {
            for (int level = 0; level < 3; level++) {
                int finalLevel = level;
                int finalCol = col;
                findViewById(InnerThread.buttonIds[col * 3 + level]).setOnClickListener((btn) -> {
                    if(gridStatus.get()) {
                        for (int col2 = 0; col2 < 9; col2++) {
                            for (int level2 = 0; level2 < 3; level2++) {
                                if(app.getGridState(level2, col2) == GridState.Targeted) {
                                    app.setGridState(level2, col2, GridState.NotFilled);
                                }
                            }
                        }
                    }
                    app.setGridState(finalLevel, finalCol, gridStatus.get() ? app.getGridState(finalLevel, finalCol) == GridState.Placing ? GridState.Filled : GridState.Targeted : app.getGridState(finalLevel, finalCol) == GridState.Filled ? GridState.NotFilled : GridState.Filled);
                });
            }
        }

        this.findViewById(R.id.confirm_button).setOnClickListener((btn) -> {
            AtomicBoolean status = new AtomicBoolean(false);
            for (int col3 = 0; col3 < 9; col3++) {
                for (int level3 = 0; level3 < 3; level3++) {
                    if(app.getGridState(level3, col3) == GridState.Targeted) {
                        app.setGridState(level3, col3, GridState.Placing);
                        try {
                            app.bridge.publish("level", level3);
                            app.bridge.publish("column", col3);
                            status.set(!status.get());
                            app.bridge.publish("Confirmed", status.get());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
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
        } catch (InterruptedException x) {
            throw new RuntimeException(x);
        }
        super.onDestroy();
    }
}