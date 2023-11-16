package com.app.autocallreply.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.autocallreply.Adapter.NumberAdapter;
import com.app.autocallreply.Broadcast_Receiver.PhonecallReceiver;
import com.app.autocallreply.Models.NumberModel;
import com.app.autocallreply.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayList<NumberModel> list;
    NumberAdapter numberAdapter;
    String phone_num;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if ((ContextCompat.checkSelfPermission(MainActivity.this,android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,android.Manifest.permission.READ_PHONE_STATE) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ANSWER_PHONE_CALLS) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.SEND_SMS)){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_PHONE_STATE,Manifest.permission.ANSWER_PHONE_CALLS,Manifest.permission.SEND_SMS}, 74);
            }else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_PHONE_STATE,Manifest.permission.ANSWER_PHONE_CALLS,Manifest.permission.SEND_SMS}, 74);
            }
        }
        list=new ArrayList<>();
        sharedPreferences=getSharedPreferences("switchstate",MODE_PRIVATE);
        binding.switch1.setChecked(sharedPreferences.getBoolean("value",true));

        numberAdapter=new NumberAdapter(list);
        binding.recyclerView.setAdapter(numberAdapter);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent=getIntent();
        if (intent!=null){
            phone_num= intent.getStringExtra("number");
            if(phone_num!=null){
                Toast.makeText(this, phone_num, Toast.LENGTH_LONG).show();
                sendSMS(phone_num,"Busy");
                list.add(new NumberModel(phone_num));
                numberAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Not identified", Toast.LENGTH_SHORT).show();
            }
        }

        binding.switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editor=getSharedPreferences("switchstate",MODE_PRIVATE).edit();
                editor.putBoolean("value",true);
                editor.apply();
                binding.switch1.setChecked(true);
                PackageManager pm  = MainActivity.this.getPackageManager();
                ComponentName componentName = new ComponentName(MainActivity.this, PhonecallReceiver.class);
                pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
                Toast.makeText(MainActivity.this, "Service enabled.", Toast.LENGTH_SHORT).show();
            } else {
                editor=getSharedPreferences("switchstate",MODE_PRIVATE).edit();
                editor.putBoolean("value",false);
                editor.apply();
                binding.switch1.setChecked(false);
                PackageManager pm  = MainActivity.this.getPackageManager();
                ComponentName componentName = new ComponentName(MainActivity.this, PhonecallReceiver.class);
                pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                Toast.makeText(MainActivity.this, "Service disabled.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.addNumber.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int[] textLocation = new int[2];
                binding.addNumber.getLocationOnScreen(textLocation);
                if (event.getRawX()<=textLocation[0]+binding.addNumber.getTotalPaddingLeft()) {
                    // Left drawable was tapped
                    return true;
                }
                if (event.getRawX()>=textLocation[0]+binding.addNumber.getWidth()-binding.addNumber.getTotalPaddingRight()){
                    Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
        //            SmsManager smsManager = getApplicationContext().getSystemService(SmsManager.class);
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage(), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 74) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
                        && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED)
                        && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "All permissions granted.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "All permissions denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}