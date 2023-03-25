package com.example.projectowen;

import android.app.Application;

import net.ffst.adbpotato.Bridge;

import java.io.IOException;
import java.util.Arrays;

public class JoystickApp extends Application {

    protected Bridge bridge;
    private final GridState[] states = new GridState[9 * 3];

    public GridState getGridState(int level, int col) {
        return states[col * 3 + level];
    }

    public void setGridState(int level, int col, GridState state) {
        states[col * 3 + level] = state;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Arrays.fill(states, GridState.NotFilled);
        try {
            bridge = new Bridge(Bridge.Side.Android);
            bridge.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        bridge.end();
        try {
            bridge.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
