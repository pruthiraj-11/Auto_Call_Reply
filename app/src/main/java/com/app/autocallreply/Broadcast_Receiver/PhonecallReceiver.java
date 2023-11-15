package com.app.autocallreply.Broadcast_Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.app.autocallreply.Activities.MainActivity;

import java.util.Objects;

public class PhonecallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            Toast.makeText(context, "Incoming call...", Toast.LENGTH_SHORT).show();
            String savedNumber = Objects.requireNonNull(intent.getExtras()).getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (tm != null) {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                tm.endCall();
            }
            if (savedNumber !=null){
                Intent intent1=new Intent(context, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("number", savedNumber);
                context.startActivity(intent1);
            }
        } else if (Objects.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE), TelephonyManager.EXTRA_STATE_IDLE)) {
                //perform main work
        }
    }

}
