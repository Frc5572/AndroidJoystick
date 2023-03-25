package com.example.projectowen;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import net.ffst.adbpotato.Bridge;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Bridge bridge = new Bridge();
    }

}