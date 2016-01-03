package edu.bluejack151.gafp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {


    Firebase firebase;

    String frequency = null;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

    }

    public void save(View view){

        if(frequency == null) {
            Toast.makeText(SettingActivity.this, "Pick time!", Toast.LENGTH_SHORT).show();
            return;
        }
        firebase.child("users/"+firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> values = new HashMap<String, Object>();
                values.put("frequency",frequency);

                Map<String,Object> settings = new HashMap<String, Object>();
                values.put("settings",values);



                firebase.child("users/"+firebase.getAuth().getUid()).updateChildren(settings);
                Toast.makeText(SettingActivity.this, "Edit setting success!", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                finish();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void eight(View view){
        frequency = "8";
    }

    public void three(View view){
        frequency = "15";
    }
    public void seven(View view){
        frequency = "19";

    }

    public void disableReceiver(){
        ComponentName receiver = new ComponentName(getApplicationContext(), MainBootReceiver.class);
        PackageManager pm = getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(){


        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), MainBootReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

}
