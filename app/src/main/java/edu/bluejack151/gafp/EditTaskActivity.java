package edu.bluejack151.gafp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditTaskActivity extends AppCompatActivity {

    Firebase firebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

        String title = getIntent().getStringExtra("title");
        final String date = getIntent().getStringExtra("date");

        firebase.child("users/"+firebase.getAuth().getUid()+"/tasks/"+date+"/"+title).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView titleEditText = (TextView)findViewById(R.id.titleEditText);
                TextView descriptionEditText = (TextView)findViewById(R.id.descriptionEditText);
                DatePicker dp = (DatePicker)findViewById(R.id.datePicker2);


                Date d = null;
                try {
                    d = new SimpleDateFormat("dd-MM-yyyy").parse(date);
                }catch(Exception e){}
                titleEditText.setText(dataSnapshot.getKey());
                descriptionEditText.setText(dataSnapshot.child("description").getValue().toString());
                dp.init(d.getYear(), d.getMonth(), d.getDay(), new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public void edit(View view){
        if(((TextView)findViewById(R.id.titleEditText)).getText().toString().isEmpty()){
            Toast.makeText(EditTaskActivity.this, "Title must be filled!", Toast.LENGTH_SHORT).show();
        }else if(((TextView)findViewById(R.id.descriptionEditText)).getText().toString().isEmpty()){
            Toast.makeText(EditTaskActivity.this, "Description must be filled!", Toast.LENGTH_SHORT).show();
        }else{

            Date now = Calendar.getInstance().getTime();

            DatePicker dp = (DatePicker) findViewById(R.id.datePicker2);
            Date deadline = new Date(dp.getYear(),dp.getMonth(),dp.getDayOfMonth());

            if(now.after(deadline)){
                Toast.makeText(EditTaskActivity.this, "Deadline must be in the future!", Toast.LENGTH_SHORT).show();
                return;
            }else{
                final String date = new SimpleDateFormat("dd-MM-yyyy").format(deadline);

                Map<String, Object> values = new HashMap<String, Object>();
                values.put("description",((EditText)findViewById(R.id.descriptionEditText)).getText().toString());

                Map<String, Object> data = new HashMap<String, Object>();
                data.put(((EditText) findViewById(R.id.titleEditText)).getText().toString(), values);


                firebase.child("users/"+firebase.getAuth().getUid()+"/tasks/"+getIntent().getStringExtra("date")
                        +"/"+getIntent().getStringExtra("title")).removeValue();

                firebase.child("users/"+firebase.getAuth().getUid()+"/tasks/"+date).updateChildren(data);

                Toast.makeText(EditTaskActivity.this, "Task Edited!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), TimetableActivity.class));
            }
        }
    }

    public void delete(View view){

        firebase.child("users/"+firebase.getAuth().getUid()+"/tasks/"+getIntent().getStringExtra("date")
                +"/"+getIntent().getStringExtra("title")).removeValue();
        Toast.makeText(EditTaskActivity.this, "Task Deleted!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), TimetableActivity.class));
    }

}
