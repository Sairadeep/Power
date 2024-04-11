package com.turbotech.power

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.turbotech.power.ui.theme.PowerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PowerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PowerSOS()
                }
            }
        }
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission to be prompted
            ActivityCompat.requestPermissions(
                this@MainActivity as Activity,
                arrayOf(Manifest.permission.CALL_PHONE),
                100
            )
        } else {
            Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT)
                .show()
        }
    }
}

@Composable
fun PowerSOS() {
    lateinit var timer: CountDownTimer
    val context = LocalContext.current
    val powerClick = remember { mutableIntStateOf(1) }
    val screenState = remember {
        mutableStateOf(false)
    }
    val timerState = remember {
        mutableStateOf(false)
    }
    val callStatus = remember {
        mutableStateOf(false)
    }
    val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            powerClick.intValue++
            if (intent != null) {
                screenState.value = intent.action == Intent.ACTION_SCREEN_ON
            }
            Log.d(
                "ScreenedValueState",
                "${powerClick.intValue}, screenState: ${screenState.value}"
            )

            if (!timerState.value) timer.start()

        }
    }

    timer = object : CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            if ((millisUntilFinished / 1000) <= 5 && powerClick.intValue == 3) {
                Log.d(
                    "ScreenedValueState",
                    "Received power click: ${powerClick.intValue} clicks of power button"
                )
                callStatus.value = true
                timer.cancel()
                powerClick.intValue = 0
            } else if (powerClick.intValue > 5) {
                powerClick.intValue = 0
                callStatus.value = false
            }
            timerState.value = false
        }

        override fun onFinish() {
            powerClick.intValue = 0
        }

    }

    // Register and Unregister Receiver
    DisposableEffect(context) {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    if (callStatus.value) {
        val phoneNumber = 999999999
        val intentToCall = Intent(Intent.ACTION_CALL)
        intentToCall.data = Uri.parse("tel: $phoneNumber")
        context.startActivity(intentToCall)
        callStatus.value = false
    } else {
        Toast.makeText(context, "Call is already placed", Toast.LENGTH_SHORT).show()
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PowerTheme {
        PowerSOS()
    }
}