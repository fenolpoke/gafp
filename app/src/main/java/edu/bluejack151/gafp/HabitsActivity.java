package edu.bluejack151.gafp;

import android.content.Intent;
import android.os.Bundle;
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
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HabitsActivity extends AppCompatActivity {

    Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);

        ImageButton addButton = (ImageButton) findViewById(R.id.addButton);

        ((TextView) findViewById(R.id.titleTextView)).setText("Habits");
//        addButton.setText("Add habit");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AddHabitActivity.class));
            }
        });

//        getFragmentManager().beginTransaction().remove().commit();

        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");
        firebase.child("users/" + firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                DataSnapshot habits = dataSnapshot.child("habits");
                for (final DataSnapshot type : habits.getChildren()) {
                    for (final DataSnapshot habit : type.getChildren()) {
                        View habitL = LayoutInflater.from(getApplicationContext()).inflate(R.layout.habit_layout, null);

                        ((TextView) habitL.findViewWithTag("frequency")).setText(type.getKey());
                        ((TextView) habitL.findViewWithTag("title")).setText(habit.getKey());
                        ((TextView) habitL.findViewWithTag("description")).setText(habit.child("description").getValue().toString());

                        CheckBox cb = (CheckBox) habitL.findViewWithTag("check");


                        ((TextView) habitL.findViewWithTag("title")).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(getApplicationContext(), EditHabitActivity.class);
                                i.putExtra("type", type.getKey());
                                i.putExtra("title", habit.getKey());
                                startActivity(i);
                            }
                        });

                        if (habit.hasChild("done") && habit.child("done").getValue().toString().equalsIgnoreCase("true")) {
                            cb.setEnabled(false);
                            cb.setChecked(true);
                        }else {

                            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {


                                        String last = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
                                        int streak = Integer.parseInt(habit.child("streak").getValue().toString()) + 1;
                                        int best = Integer.parseInt(habit.child("best").getValue().toString());

                                        if(best < streak) best = streak;

                                        Map<String,Object> habitV = new HashMap<String, Object>();
                                        habitV.put("last", last);
                                        habitV.put("best", best);
                                        habitV.put("streak", streak);
                                        habitV.put("done", true);

                                        firebase.child("users/"
                                                + firebase.getAuth().getUid()
                                                + "/habits/"
                                                + type.getKey()+"/"+habit.getKey())
                                                .updateChildren(habitV);

                                        int point = Integer.parseInt(dataSnapshot.child("point").getValue().toString());

                                        firebase.child("users/"
                                                + firebase.getAuth().getUid()
                                                + "/point").setValue(point + streak * 10);


                                        buttonView.setEnabled(false);
                                        Toast.makeText(HabitsActivity.this, "Habit finished!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                        ((LinearLayout) findViewById(R.id.habits)).addView(habitL);

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
//                    View habit = LayoutInflater.from(getApplicationContext()).inflate(R.layout.habit_layout, null);
//
//
//                    ((TextView) habit.findViewWithTag("frequency")).setText(((Date) (ds.child("date").getValue())).compareTo(Calendar.getInstance().getTime()));
//                    ((TextView) habit.findViewWithTag("title")).setText(ds.child("title").getValue().toString());
//                    ((TextView) habit.findViewWithTag("description")).setText(ds.child("description").toString());
//                    ((CheckBox) habit.findViewWithTag("check")).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                        @Override
//                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                            if (isChecked) {
//                                firebase.child("users/"
//                                        + firebase.getAuth().getAuth().get("name")
//                                        + "/habits/"
//                                        + key).setValue(true);
//                                Toast.makeText(HabitsActivity.this, "Habit finished!", Toast.LENGTH_SHORT).show();
//                            }
//
//                        }
//                    });
//
//                    ((LinearLayout) findViewById(R.id.habits)).addView(habit);
//
//
//                }
//
//            }
//        });
    }
}
