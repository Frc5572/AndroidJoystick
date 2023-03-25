package com.example.projectowen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.widget.ImageButton;

import net.ffst.adbpotato.Bridge;

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


}