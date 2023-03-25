package com.example.projectowen;

import android.graphics.Color;

public enum GridState {

    NotFilled(Color.DKGRAY),
    Filled(Color.CYAN),
    Targeted(Color.RED);

    public final int backgroundColor;

    GridState(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

}
