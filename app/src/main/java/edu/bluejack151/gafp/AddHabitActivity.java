package edu.bluejack151.gafp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.util.HashMap;
import java.util.Map;

public class AddHabitActivity extends AppCompatActivity {

    Firebase firebase;
    private String type = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

        ImageButton addButton = (ImageButton)findViewById(R.id.addButton);

        ((TextView)findViewById(R.id.titleTextView)).setText("Add New Habit");
        //addButton.setText("Add new habit");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(((EditText)findViewById(R.id.titleEditText)).getText().toString().isEmpty()){
                    Toast.makeText(AddHabitActivity.this, "Title must be filled!", Toast.LENGTH_SHORT).show();
                    return;
                }else if(((EditText)findViewById(R.id.descriptionEditText)).getText().toString().isEmpty()){
                    Toast.makeText(AddHabitActivity.this, "Description must be filled!", Toast.LENGTH_SHORT).show();
                    return;
                }else if(type == null){
                    Toast.makeText(AddHabitActivity.this, "Type must be filled!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(AddHabitActivity.this, "making habit...", Toast.LENGTH_SHORT).show();

                Map<String, Object> values = new HashMap<String, Object>();
                values.put("description",((EditText)findViewById(R.id.descriptionEditText)).getText().toString());
                values.put("streak",0);
                values.put("best",0);

                Map<String, Object> data = new HashMap<String, Object>();
                data.put(((EditText)findViewById(R.id.titleEditText)).getText().toString(),values);


                firebase.child("users/"+firebase.getAuth().getUid()+"/habits/"+type).updateChildren(data);

                Toast.makeText(AddHabitActivity.this, "Habit Added!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),HabitsActivity.class));
            }
        });

    }

    public void daily(View view){
        type = "daily";
    }

    public void weekly(View view){
        type = "weekly";
    }

    public void monthly(View view){
        type = "monthly";
    }

}
