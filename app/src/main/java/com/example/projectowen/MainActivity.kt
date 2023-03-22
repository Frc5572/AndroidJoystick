package com.example.projectowen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ConditionVariable
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.SocketException

class MainActivity : AppCompatActivity() {
    companion object {
        val buttons = intArrayOf(
            R.id.l0c0, R.id.l0c1, R.id.l0c2, R.id.l0c3, R.id.l0c4, R.id.l0c5, R.id.l0c6, R.id.l0c7, R.id.l0c8,
            R.id.l1c0, R.id.l1c1, R.id.l1c2, R.id.l1c3, R.id.l1c4, R.id.l1c5, R.id.l1c6, R.id.l1c7, R.id.l1c8,
            R.id.l2c0, R.id.l2c1, R.id.l2c2, R.id.l2c3, R.id.l2c4, R.id.l2c5, R.id.l2c6, R.id.l2c7, R.id.l2c8,
        )
    }

    class SocketWorker(port: Int, private var activity: Activity) : Thread() {

        private var socket = ServerSocket(port)
        private var cond = ConditionVariable()
        private var active = 0
        private var intake: Double = 0.0

        private val buttons = intArrayOf(
            R.id.l0c0, R.id.l0c1, R.id.l0c2, R.id.l0c3, R.id.l0c4, R.id.l0c5, R.id.l0c6, R.id.l0c7, R.id.l0c8,
            R.id.l1c0, R.id.l1c1, R.id.l1c2, R.id.l1c3, R.id.l1c4, R.id.l1c5, R.id.l1c6, R.id.l1c7, R.id.l1c8,
            R.id.l2c0, R.id.l2c1, R.id.l2c2, R.id.l2c3, R.id.l2c4, R.id.l2c5, R.id.l2c6, R.id.l2c7, R.id.l2c8,
        )

        var buttonToGroup = HashMap<Int, Pair<Int, Int>>()

        init {
            for(level in 0..2) {
                for (col in 0..8) {
                    buttonToGroup[buttons[level * 9 + col]] = Pair(level, col)
                }
            }
        }

        override fun run() {
            while(true) {
                try {
                    activity.runOnUiThread {
                        val textView = activity.findViewById<TextView>(R.id.connection_status)
                        textView.setText(R.string.not_connected)
                    }

                    println("Waiting for client")
                    val client = socket.accept()
                    println("Client found!")

                    activity.runOnUiThread {
                        val textView = activity.findViewById<TextView>(R.id.connection_status)
                        textView.setText(R.string.connected)
                    }

                    val input = DataInputStream(BufferedInputStream(client.getInputStream()))
                    val output = DataOutputStream(client.getOutputStream())

                    while (true) {
                        cond.block();
                        // buttons
                        if (buttonToGroup.containsKey(active)) {
                            output.writeByte(2)
                            val lc = buttonToGroup[active]
                            if (lc != null) {
                                output.writeByte(lc.first)
                                output.writeByte(lc.second)
                            } else {
                                output.writeByte(1)
                            }
                        } else {
                            output.writeByte(1)
                        }
                        // intake
                        output.writeByte(3)
                        output.writeDouble(intake)

                        output.flush()
                        cond.close();
                    }
                } catch (exc: SocketException) {
                    // Do nothing
                }
            }
        }

        fun setActive(x: Int) {
            active = x
            cond.open()
        }

        fun setIntake(x: Double) {
            intake = x
            cond.open()
        }
    }

    private fun reset() {
        buttons.forEach {
            val button = findViewById<ImageButton>(it)
            if((button.background as ColorDrawable).color != Color.DKGRAY) {
                button.setBackgroundColor(Color.CYAN)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val worker = SocketWorker(1234, this)
        worker.start()

        val rollers = findViewById<SeekBar>(R.id.rollers);
        rollers.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                worker.setIntake(((p0?.progress?.toDouble()!!) / (p0.max.toDouble())) * 2.0 - 1.0)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.setProgress(p0.max / 2, false)
                worker.setIntake(0.0)
            }

        });

        buttons.forEach {
            val button = findViewById<ImageButton>(it)
            button.setBackgroundColor(Color.DKGRAY)
            button.setOnClickListener { btn ->
                reset()
                if ((btn.background as ColorDrawable).color == Color.CYAN) {
                    btn.setBackgroundColor(Color.DKGRAY)
                } else {
                    btn.setBackgroundColor(Color.RED)
                    worker.setActive(it)
                }
            }
        }
    }
}