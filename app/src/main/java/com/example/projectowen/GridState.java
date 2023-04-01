package com.example.projectowen;

import android.graphics.Color;

public enum GridState {

    NotFilled(Color.DKGRAY),
    Filled(Color.GREEN),
    Targeted(Color.CYAN),
    Placing(Color.RED);

    public final int backgroundColor;

    GridState(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

}
