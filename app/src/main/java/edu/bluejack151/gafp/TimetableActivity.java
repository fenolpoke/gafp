package edu.bluejack151.gafp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimetableActivity extends AppCompatActivity {

    Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        ImageButton addButton = (ImageButton)findViewById(R.id.addButton);

        ((TextView)findViewById(R.id.titleTextView)).setText("Timetable");
//        addButton.setText("Add task");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AddTaskActivity.class));
            }
        });

        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

        firebase.child("users/" + firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                DataSnapshot tasks = dataSnapshot.child("tasks");
                for (final DataSnapshot deadline : tasks.getChildren()) {
                    for (final DataSnapshot task : deadline.getChildren()) {
                        Date now, date;
                        now = date = null;
                        try {
                            now = Calendar.getInstance().getTime();
                            date = new SimpleDateFormat("dd-MM-yyyy").parse(deadline.getKey());
                        } catch (Exception e) {
                        }

                        final long remaining = TimeUnit.DAYS.convert(date.getTime() - now.getTime(), TimeUnit.MILLISECONDS);

                        View timetable = LayoutInflater.from(getApplicationContext()).inflate(R.layout.timetable_layout, null);

                        ((TextView) timetable.findViewWithTag("remaining")).setText(remaining >= 0 ? (remaining + 1)+ "" : "-");
                        ((TextView) timetable.findViewWithTag("date")).setText(deadline.getKey());
                        ((TextView) timetable.findViewWithTag("title")).setText(task.getKey());
                        ((TextView) timetable.findViewWithTag("description")).setText(task.child("description").getValue().toString());


                        CheckBox cb = (CheckBox) timetable.findViewWithTag("check");

                        if (task.hasChild("done") || remaining < 0) {
                            cb.setEnabled(false);
                            cb.setChecked(true);
                        } else {
                            ((TextView) timetable.findViewWithTag("title")).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(getApplicationContext(), EditTaskActivity.class);
                                    i.putExtra("date", deadline.getKey());
                                    i.putExtra("title", task.getKey());
                                    startActivity(i);
                                }
                            });

                            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        firebase.child("users/"
                                                + firebase.getAuth().getUid()
                                                + "/tasks/"
                                                + deadline.getKey() + "/" + task.getKey() + "/done")
                                                .setValue(true);

                                        int point = Integer.parseInt(dataSnapshot.hasChild("point") ? dataSnapshot.child("point").getValue().toString() : "0");

                                        firebase.child("users/"
                                                + firebase.getAuth().getUid()
                                                + "/point").setValue(point + remaining * 10);


                                        buttonView.setEnabled(false);
                                        Toast.makeText(TimetableActivity.this, "Task finished!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                        ((LinearLayout) findViewById(R.id.timetable)).addView(timetable);

                    }
                }
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
//                .runTransaction(new Transaction.Handler() {
//            @Override
//            public Transaction.Result doTransaction(MutableData mutableData) {
//                return null;
//            }
//
//            @Override
//            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
//                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//
//                    final String key = ds.getKey();
//
//                    View timetable = LayoutInflater.from(getApplicationContext()).inflate(R.layout.timetable_layout, null);
//
//
//                    ((TextView) timetable.findViewWithTag("remaining")).setText(((Date) (ds.child("date").getValue())).compareTo(Calendar.getInstance().getTime()));
//                    ((TextView) timetable.findViewWithTag("date")).setText(ds.child("date").getValue().toString());
//                    ((TextView) timetable.findViewWithTag("title")).setText(ds.child("title").getValue().toString());
//                    ((TextView) timetable.findViewWithTag("description")).setText(ds.child("description").toString());
//                    ((CheckBox) timetable.findViewWithTag("check")).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                        @Override
//                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                            if (isChecked) {
//
//                                firebase.child("users/"
//                                        + firebase.getAuth().getUid()
//                                        + "/tasks/"
//                                        + date + "/" + task.getKey() + "/done").setValue(true);
//
//
//                                int point = Integer.parseInt(dataSnapshot.child("point").getValue().toString());
//
//                                firebase.child("users/"
//                                        + firebase.getAuth().getUid()
//                                        + "/point").setValue(point + 10);
//
//
//                                Toast.makeText(HomeActivity.this, "Task finished!", Toast.LENGTH_SHORT).show();
//                                TimeUnit.DAYS.convert(now.getTime() - last.getTime(), TimeUnit.MILLISECONDS)
//
//                                Toast.makeText(TimetableActivity.this, "Task finished!", Toast.LENGTH_SHORT).show();
//                            }
//
//                        }
//                    });
//
//                    ((LinearLayout) findViewById(R.id.timetable)).addView(timetable);
//
//
//                }
//
//            }
//        });

    }
}
