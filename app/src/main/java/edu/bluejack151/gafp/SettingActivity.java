package edu.bluejack151.gafp;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

    }

    public void save(View view){
        firebase.child("users/"+firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> values = new HashMap<String, Object>();
                values.put("frequency",frequency);

                Map<String,Object> settings = new HashMap<String, Object>();
                values.put("settings",values);



                firebase.child("users/"+firebase.getAuth().getUid()).updateChildren(settings);
                Toast.makeText(SettingActivity.this, "Edit setting success!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void one(View view){
        frequency = "1";
    }

    public void three(View view){
        frequency = "3";
    }
    public void seven(View view){
        frequency = "7";

    }
}
