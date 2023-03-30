package com.example.projectowen;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AutoActivity extends AppCompatActivity {
    public static class InnerThread extends Thread {

        private final AutoActivity activity;
        public InnerThread(AutoActivity activity) {
            this.activity = activity;
        }

        @Override
        public void run() {

        }

        public void end() { }
    }

    private InnerThread thread;
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_score);
        thread = new InnerThread(this);

        this.findViewById(R.id.gridTab).setOnClickListener((btn) -> {
            setContentView(R.layout.activity_score);
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
}

