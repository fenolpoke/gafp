package edu.bluejack151.gafp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class AddTaskActivity extends AppCompatActivity {

    Firebase firebase;

    int year = 0, month = 0, day = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

        ImageButton addButton = (ImageButton)findViewById(R.id.addButton);

        ((TextView)findViewById(R.id.titleTextView)).setText("Add New Task");
//        addButton.setText("Add new task");

        DatePicker dp = (DatePicker) findViewById(R.id.datePicker);
        dp.init(1992, 2, 2, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Toast.makeText(AddTaskActivity.this, new SimpleDateFormat("dd-MM-yyyy").format(new Date(year,monthOfYear,dayOfMonth)), Toast.LENGTH_SHORT).show();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddTaskActivity.this, "making task...", Toast.LENGTH_SHORT).show();

//                ((DatePicker)findViewById(R.id.datePicker)).get

                if(((EditText)findViewById(R.id.titleEditText)).getText().toString().isEmpty()){
                    Toast.makeText(AddTaskActivity.this, "Title must be filled!", Toast.LENGTH_SHORT).show();
                    return;
                }else if(((EditText)findViewById(R.id.descriptionEditText)).getText().toString().isEmpty()){
                    Toast.makeText(AddTaskActivity.this, "Description must be filled!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Date now = Calendar.getInstance().getTime();

                DatePicker dp = (DatePicker) findViewById(R.id.datePicker);
                Date deadline = new Date(dp.getYear(),dp.getMonth(),dp.getDayOfMonth());

                if(now.after(deadline)){
                    Toast.makeText(AddTaskActivity.this, "Deadline must be in the future!", Toast.LENGTH_SHORT).show();
                    return;
                }//asd
                final String date = new SimpleDateFormat("dd-MM-yyyy").format(deadline);

                Map<String, Object> values = new HashMap<String, Object>();
                values.put("description", ((EditText) findViewById(R.id.descriptionEditText)).getText().toString());

                final Map<String, Object> data = new HashMap<String, Object>();
                data.put(((EditText) findViewById(R.id.titleEditText)).getText().toString(), values);

                firebase.child("users/" + firebase.getAuth().getUid() + "/tasks/" + date).updateChildren(data);

                firebase.child("users/"+firebase.getAuth().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot userPoint = dataSnapshot.child("point");
                        DataSnapshot userMoney = dataSnapshot.child("money");

                        Integer currentPoint = Integer.parseInt(userPoint.getValue().toString());
                        Integer currentMoney = Integer.parseInt(userMoney.getValue().toString());
                        currentPoint += 10;
                        currentMoney += 5;

                        final Map<String, Object> newPoint = new HashMap<String, Object>();
                        final Map<String, Object> newMoney = new HashMap<String, Object>();
                        newPoint.put("point",currentPoint);
                        newMoney.put("money",currentMoney);

                        firebase.child("users/" + firebase.getAuth().getUid()).updateChildren(newPoint);
                        firebase.child("users/" + firebase.getAuth().getUid()).updateChildren(newMoney);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

                Toast.makeText(AddTaskActivity.this, "Task Added!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), TimetableActivity.class));
            }
        });


    }

}
