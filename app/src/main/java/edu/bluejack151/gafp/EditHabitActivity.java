package edu.bluejack151.gafp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditHabitActivity extends AppCompatActivity {


    Firebase firebase;
    String type = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_habit);

        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");


        String title = getIntent().getStringExtra("title");
        final String type = getIntent().getStringExtra("type");

        firebase.child("users/"+firebase.getAuth().getUid()+"/habits/"+type+"/"+title).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Toast.makeText(EditHabitActivity.this, type, Toast.LENGTH_SHORT).show();

                TextView titleEditText = (TextView)findViewById(R.id.titleEditText);
                TextView descriptionEditText = (TextView)findViewById(R.id.descriptionEditText);

                if(type.equalsIgnoreCase("daily"))((RadioButton)findViewById(R.id.dailyRadioButton)).setChecked(true);
                if(type.equalsIgnoreCase("weekly"))((RadioButton)findViewById(R.id.weeklyRadioButton)).setChecked(true);
                if(type.equalsIgnoreCase("monthly"))((RadioButton)findViewById(R.id.monthlyRadioButton)).setChecked(true);


                titleEditText.setText(dataSnapshot.getKey());
                descriptionEditText.setText(dataSnapshot.child("description").getValue().toString());

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }

    public void ed(View view){
        if(((EditText)findViewById(R.id.titleEditText)).getText().toString().isEmpty()){
            Toast.makeText(EditHabitActivity.this, "Title must be filled!", Toast.LENGTH_SHORT).show();
            return;
        }else if(((EditText)findViewById(R.id.descriptionEditText)).getText().toString().isEmpty()){
            Toast.makeText(EditHabitActivity.this, "Description must be filled!", Toast.LENGTH_SHORT).show();
            return;
        }else if(type == null){
            Toast.makeText(EditHabitActivity.this, "Type must be filled!", Toast.LENGTH_SHORT).show();
            return;
        }



        Map<String, Object> values = new HashMap<String, Object>();
        values.put("description",((EditText)findViewById(R.id.descriptionEditText)).getText().toString());
        values.put("streak",0);
        values.put("best",0);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put(((EditText) findViewById(R.id.titleEditText)).getText().toString(), values);


        firebase.child("users/"+firebase.getAuth().getUid()+"/habits/"+type).updateChildren(data);

        firebase.child("users/"+firebase.getAuth().getUid()+"/habits/"+getIntent().getStringExtra("type")+"/"+getIntent().getStringExtra("title")).removeValue();

        Toast.makeText(EditHabitActivity.this, "Habit Edited!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), HabitsActivity.class));

    }
    public void del(View view){
        firebase.child("users/"+firebase.getAuth().getUid()+"/habits/"+getIntent().getStringExtra("type")+"/"+getIntent().getStringExtra("title")).removeValue();

        Toast.makeText(EditHabitActivity.this, "Habit Deleted!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), HabitsActivity.class));

    }
    public void month(View view){
        type = "monthly";
    }
    public void week(View view){
        type = "weekly";
    }
    public void day(View view){
        type = "daily";
    }
}
