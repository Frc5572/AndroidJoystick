package com.example.projectowen;

import android.content.Intent;
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
        setContentView(R.layout.activity_auto);
        thread = new InnerThread(this);
        thread.start();

        this.findViewById(R.id.gridTab).setOnClickListener((btn) -> {
            startActivity(new Intent(AutoActivity.this, ScoringActivity.class));
            finish();
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

