package com.app.autocallreply.Broadcast_Receiver;


import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.app.autocallreply.Activities.MainActivity;
import com.app.autocallreply.Models.NumberModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class PhonecallReceiver extends BroadcastReceiver {

    Context context;
    SharedPreferences sharedPreferences;
    ArrayList<NumberModel> list;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
//            Toast.makeText(context, "Incoming call...", Toast.LENGTH_SHORT).show();
            String savedNumber = Objects.requireNonNull(intent.getExtras()).getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (tm != null) {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                list=new ArrayList<>();
                sharedPreferences = context.getSharedPreferences("appInfo", MODE_PRIVATE);
                Gson gson = new Gson();
                String json = sharedPreferences.getString("numberList", null);
                Type type = new TypeToken<ArrayList<NumberModel>>() {}.getType();
                list= gson.fromJson(json, type);
                if (list != null && !list.isEmpty()) {
                    for (NumberModel numberModel:list){
                        if (numberModel.getNumber().equals(savedNumber)){
                            tm.endCall();
                            sendSMS(savedNumber,"Busy");
                        }
                    }
                }
            }
//            if (savedNumber !=null){
//                Intent intent1=new Intent(context, MainActivity.class);
//                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent1.putExtra("number", savedNumber);
//                context.startActivity(intent1);
//            }
        } else if (Objects.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE), TelephonyManager.EXTRA_STATE_IDLE)) {
                //perform main work
        }
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(context,ex.getMessage(), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }
}
