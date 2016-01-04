package edu.bluejack151.gafp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    Firebase firebase;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ((TextView) findViewById(R.id.goodday)).
                setText("Loading..");
        final TextView taskText = new TextView(getApplicationContext());
        taskText.setTextColor(Color.BLACK);
        taskText.setText("loading tasks...");
        ((LinearLayout) findViewById(R.id.tasks)).addView(taskText);

        final TextView habitText = new TextView(getApplicationContext());
        habitText.setTextColor(Color.BLACK);
        ((LinearLayout) findViewById(R.id.habits)).addView(habitText);
        habitText.setText("Loading habits..");

        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(getApplicationContext(), MainBootReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        DataSnapshot ds = new DataSnapshot(firebase, )
//                firebase.child("users/"+firebase.getAuth().getAuth().get("name")+"/tasks");


        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

        final Vector<String> newRank = new Vector<String>();

        firebase.child("users/" + firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int hour = 8;
                if (dataSnapshot.hasChild("settings/notification")) {
                    hour = Integer.parseInt(dataSnapshot.child("settings/notification").getValue().toString());
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);

                // With setInexactRepeating(), you have to use one of the AlarmManager interval
                // constants--in this case, AlarmManager.INTERVAL_DAY.
                alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, alarmIntent);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        firebase.child("users/" + firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                final String date = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());

                String name = dataSnapshot.hasChild("username") ? dataSnapshot.child("username").getValue().toString() : "User";

                //rank
                final String rank = dataSnapshot.hasChild("rank") ?  dataSnapshot.child("rank").getValue().toString() : "Amateur";
                final String nextLevel =dataSnapshot.hasChild("point") ?  dataSnapshot.child("point").getValue().toString() : "0";
                firebase.child("rank").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot rankChild : dataSnapshot.getChildren())
                        {
                            Integer rankPoint = Integer.parseInt(rankChild.getValue().toString());
                            Integer currPoint = Integer.parseInt(nextLevel);
                            if(currPoint < rankPoint){
                                newRank.add(rankChild.getKey().toString());
                            }
                        }
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });



                String money =dataSnapshot.hasChild("money") ?  dataSnapshot.child("money").getValue().toString() : "0";

                //setAvatar
                ImageView avatar = (ImageView) findViewById(R.id.avatarImageView);
                DataSnapshot ava = dataSnapshot.child("avatar");
                String avatarString = "";
                for (final DataSnapshot activeAvatar : ava.getChildren()) {
                    if (activeAvatar.getValue() == true) {
                    avatarString = activeAvatar.getKey().toString();
                    break;
                    }
                }
                if (avatarString == "") avatarString = "";
                int avaID = getResources().getIdentifier(avatarString, "drawable", getPackageName());
                if(avatar != null) avatar.setImageResource(avaID);

                //setBackground
                RelativeLayout homeLayout = (RelativeLayout) findViewById(R.id.homeLayout);
                DataSnapshot the = dataSnapshot.child("themes");
                String themeString = "";
                for (final DataSnapshot activeTheme : the.getChildren()) {
                    if (activeTheme.getValue() == true) {
                        themeString = activeTheme.getKey().toString();
                        break;
                    }
                }
                int theID = getResources().getIdentifier(themeString, "drawable", getPackageName());
                if(homeLayout != null) homeLayout.setBackgroundResource(theID);

                TextView usernameText = (TextView) findViewById(R.id.usernameTextView);
                TextView rankText = (TextView) findViewById(R.id.rankTextView);
                TextView nextlevelText = (TextView) findViewById(R.id.nextLevelTextView);
                TextView moneyText = (TextView) findViewById(R.id.moneyTextView);

                usernameText.setText(name);
                if(newRank.size() < 1) rankText.setText("Amateur");
                else rankText.setText(newRank.get(0));
                nextlevelText.setText("Points : " + nextLevel);
                moneyText.setText(money);

                ((TextView) findViewById(R.id.goodday)).
                setText("What's up, " + name);

                boolean done = false;

                if (dataSnapshot.child("tasks").exists()) {
                    DataSnapshot tasks = dataSnapshot.child("tasks");
                    if (tasks.hasChild(date)) {
                        DataSnapshot today = tasks.child(date);
                        for (final DataSnapshot task : today.getChildren()) {
                            if (!task.hasChild("done")) {
                                View deadline = LayoutInflater.from(getApplicationContext()).inflate(R.layout.deadline, null);

                                ((TextView) deadline.findViewWithTag("text")).setText(date);
                                ((CheckBox) deadline.findViewWithTag("check")).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    firebase.child("users/"
                                            + firebase.getAuth().getUid()
                                            + "/tasks/"
                                            + date + "/" + task.getKey() + "/done").setValue(true);

                                    int point = Integer.parseInt(dataSnapshot.child("point").getValue().toString());

                                    firebase.child("users/"
                                            + firebase.getAuth().getUid()
                                            + "/point").setValue(point + 10);


                                    Toast.makeText(HomeActivity.this, "Task finished!", Toast.LENGTH_SHORT).show();
                                }

                        }
                        });

                                ((LinearLayout) findViewById(R.id.tasks)).addView(deadline);
                                done = true;
                            }
                        }
                    }
                }
                taskText.setText(done ? "":"You have no tasks today");


                if (dataSnapshot.child("habits").exists()) {
                    DataSnapshot habits = dataSnapshot.child("habits");
                    done = false;
                    for (final DataSnapshot type : habits.getChildren()) {

                        for (final DataSnapshot habit : type.getChildren()) {
                            final Date now = Calendar.getInstance().getTime();
                            Date last = new Date(now.getYear() - 1, now.getMonth(), now.getDay());
                            final int span = type.getKey().equalsIgnoreCase("daily") ? 1 : type.getKey().equalsIgnoreCase("weekly") ? 7 : 30;

                            if (habit.hasChild("last")) {
                                try {
                                last = new SimpleDateFormat("dd-MM-yyyy").parse(habit.child("last").getValue().toString());
                                } catch (Exception e) {}
                            }

                            if (TimeUnit.DAYS.convert(now.getTime() - last.getTime(), TimeUnit.MILLISECONDS) > span) {

                                firebase.child("users/"
                                        + firebase.getAuth().getUid()
                                        + "/habits/"
                                        + type.getKey() + "/" + habit.getKey() + "/done").setValue("false");


                                View deadline = LayoutInflater.from(getApplicationContext()).inflate(R.layout.deadline, null);

                                ((TextView) deadline.findViewWithTag("text")).setText(habit.getKey());
                                ((CheckBox) deadline.findViewWithTag("check")).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {

                                            String last = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
                                            int streak = Integer.parseInt(habit.hasChild("streak") ? habit.child("streak").getValue().toString() : "0") + 1;
                                            int best = Integer.parseInt(habit.hasChild("best") ? habit.child("best").getValue().toString() : "0");

                                            if (best < streak)
                                                best = streak;

                                            Map<String, Object> habitV = new HashMap<String, Object>();
                                            habitV.put("last", last);
                                            habitV.put("best", best);
                                            habitV.put("streak", streak);
                                            habitV.put("done", true);

                                            firebase.child("users/"
                                                    + firebase.getAuth().getUid()
                                                    + "/habits/"
                                                    + type.getKey() + "/" + habit.getKey())
                                                    .updateChildren(habitV);

                                            int point = Integer.parseInt(dataSnapshot.hasChild("point") ? dataSnapshot.child("point").getValue().toString() : "0");

                                            firebase.child("users/"
                                                    + firebase.getAuth().getUid()
                                                    + "/point").setValue(point + streak * 10);


                                            buttonView.setEnabled(false);
                                            Toast.makeText(HomeActivity.this, "Habit finished!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                                ((LinearLayout) findViewById(R.id.habits)).addView(deadline);
                                done = true;
                            }

                        }
                    }
                    habitText.setText(done?"":"You have no habits today");


                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            Toast.makeText(HomeActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
            }

        );
//                        if (habits.hasChild(date)) {
//                            DataSnapshot today = habits.child(date);
//                            for (final DataSnapshot task : today.getChildren()) {
//                                if (!task.hasChild("done")) {
//                                    View deadline = LayoutInflater.from(getApplicationContext()).inflate(R.layout.deadline, null);
//
//                                    ((TextView) deadline.findViewWithTag("text")).setText(date);
//                                    ((CheckBox) deadline.findViewWithTag("check")).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                                        @Override
//                                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                                            if (isChecked) {
//                                                firebase.child("users/"
//                                                        + firebase.getAuth().getUid()
//                                                        + "/habits/"
//                                                        + date + "/" + task.getKey() + "/done").setValue(true);
//                                                Toast.makeText(HomeActivity.this, "Habit finished!", Toast.LENGTH_SHORT).show();
//                                            }
//
//                                        }
//                                    });
//
//                                    ((LinearLayout) findViewById(R.id.tasks)).addView(deadline);
//                                }
//                            }
//                        }

//                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                    if (ds.exists()
//                            && Calendar.getInstance().getTime() == new Date(ds.child("date").getValue().toString())
//                            && ds.child("finished").getValue() != null) {
//
//                        final String key = ds.getKey();
//
//
//                    } else {
//                        TextView text = new TextView(getApplicationContext());
//                        text.setText("No task today!");
//                        ((LinearLayout) findViewById(R.id.tasks)).addView(text);
//                    }
//                }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_edit_profile) {
            Toast.makeText(HomeActivity.this, "Opening edit profile..", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
        } else if (id == R.id.nav_setting) {

            Toast.makeText(HomeActivity.this, "Opening setting..", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), SettingActivity.class));
        } else if (id == R.id.nav_shop) {
            Toast.makeText(HomeActivity.this, "Opening shop..", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), ShopActivity.class));
        } else if (id == R.id.nav_log_out) {
            Toast.makeText(HomeActivity.this, "Logging out..", Toast.LENGTH_SHORT).show();
            firebase.unauth();
            Toast.makeText(HomeActivity.this, "logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        } else if (id == R.id.nav_achievements) {
            Toast.makeText(HomeActivity.this, "Opening achievement..", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), AchievementActivity.class));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void toTimetable(View view) {
        startActivity(new Intent(getApplicationContext(), TimetableActivity.class));
    }

    public void toHabits(View view) {
        startActivity(new Intent(getApplicationContext(), HabitsActivity.class));
    }

}
