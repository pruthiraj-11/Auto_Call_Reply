package com.app.autocallreply.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputFilter;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.autocallreply.Adapter.NumberAdapter;
import com.app.autocallreply.Broadcast_Receiver.PhonecallReceiver;
import com.app.autocallreply.Models.NumberModel;
import com.app.autocallreply.R;
import com.app.autocallreply.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayList<NumberModel> list;
    NumberAdapter numberAdapter;
    String phone_num;
    EditText getPhoneNum;
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
            } else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_PHONE_STATE,Manifest.permission.ANSWER_PHONE_CALLS,Manifest.permission.SEND_SMS}, 74);
            }
        }
        list=new ArrayList<>();
        sharedPreferences=getSharedPreferences("appInfo",MODE_PRIVATE);
        boolean ischecked=sharedPreferences.getBoolean("switchState",true);
        loadData();
        numberAdapter=new NumberAdapter(list);
        binding.recyclerView.setAdapter(numberAdapter);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        if (ischecked){
            binding.switch1.setChecked(true);
        } else {
            binding.switch1.setChecked(false);
            binding.addNumber.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.GONE);
        }

//        Intent intent=getIntent();
//        if (intent!=null){
//            phone_num= intent.getStringExtra("number");
//            if(phone_num!=null){
//                Toast.makeText(this, phone_num, Toast.LENGTH_LONG).show();
////                sendSMS(phone_num,"Busy");
//                list.add(new NumberModel(phone_num));
//                numberAdapter.notifyDataSetChanged();
//            } else {
//                Toast.makeText(this, "Not identified", Toast.LENGTH_SHORT).show();
//            }
//        }

        binding.switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editor=getSharedPreferences("appInfo",MODE_PRIVATE).edit();
                editor.putBoolean("switchState",true);
                editor.apply();
                PackageManager pm  = MainActivity.this.getPackageManager();
                ComponentName componentName = new ComponentName(MainActivity.this, PhonecallReceiver.class);
                pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
                binding.switch1.setChecked(true);
                binding.addNumber.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Service enabled.", Toast.LENGTH_SHORT).show();
            } else {
                editor=getSharedPreferences("appInfo",MODE_PRIVATE).edit();
                editor.putBoolean("switchState",false);
                editor.apply();
                PackageManager pm  = MainActivity.this.getPackageManager();
                ComponentName componentName = new ComponentName(MainActivity.this, PhonecallReceiver.class);
                pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                binding.switch1.setChecked(false);
                binding.addNumber.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Service disabled.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.addNumber.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int[] textLocation = new int[2];
                binding.addNumber.getLocationOnScreen(textLocation);
                if (event.getRawX()<=textLocation[0]+binding.addNumber.getTotalPaddingLeft()) {
                    return true;
                }
                if (event.getRawX()>=textLocation[0]+binding.addNumber.getWidth()-binding.addNumber.getTotalPaddingRight()){
                    AlertDialog.Builder alertPhone = new AlertDialog.Builder(this);
                    alertPhone.setCancelable(false);
                    final EditText phoneinput = new EditText(MainActivity.this);
                    phoneinput.setInputType(InputType.TYPE_CLASS_PHONE);
                    phoneinput.setFilters(new InputFilter[] {
                            new InputFilter.LengthFilter(10)
                    });
                    alertPhone.setTitle("Enter a phone number");
                    alertPhone.setView(phoneinput);
                    LinearLayout layoutName = new LinearLayout(this);
                    layoutName.setOrientation(LinearLayout.VERTICAL);
                    layoutName.addView(phoneinput);
                    alertPhone.setView(layoutName);
                    alertPhone.setPositiveButton("Add", (dialog, whichButton) -> {
                        getPhoneNum = phoneinput;
                        collectInput();
                    });
                    alertPhone.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.cancel());
                    alertPhone.setNeutralButton("Contacts", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertPhone.show();
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
    }

    public void collectInput(){
        String getInput = getPhoneNum.getText().toString();
        if (getInput ==null || getInput.trim().equals("")){
            Toast.makeText(getBaseContext(), "Field can't be empty.", Toast.LENGTH_LONG).show();
        }
        else {
            list.add(new NumberModel("+91"+getInput));
            numberAdapter.notifyDataSetChanged();
            saveData();
            Toast.makeText(MainActivity.this, "Phone number added.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveData() {
        sharedPreferences = getSharedPreferences("appInfo", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("numberList", json);
        editor.apply();
        Toast.makeText(this, "Saved Array List", Toast.LENGTH_SHORT).show();
    }

    private void loadData() {
        sharedPreferences = getSharedPreferences("appInfo", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("numberList", null);
        Type type = new TypeToken<ArrayList<NumberModel>>() {}.getType();
        list= gson.fromJson(json, type);
        if (list == null) {
            list = new ArrayList<>();
        }
    }

//    public void sendSMS(String phoneNo, String msg) {
//        try {
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
//            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
//        } catch (Exception ex) {
//            Toast.makeText(getApplicationContext(),ex.getMessage(), Toast.LENGTH_SHORT).show();
//            ex.printStackTrace();
//        }
//    }

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