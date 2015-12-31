package edu.bluejack151.gafp;

import android.content.Intent;
import android.content.res.Resources;
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
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    Firebase firebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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

        firebase.child("users/" + firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                  @Override
                                                                                                  public void onDataChange(final DataSnapshot dataSnapshot) {
                                                                                                      final String date = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());

                                                                                                      String name = dataSnapshot.hasChild("username") ? dataSnapshot.child("username").getValue().toString() : "User";
                                                                                                      String rank = dataSnapshot.child("rank").getValue().toString();
                                                                                                      String nextLevel = dataSnapshot.child("point").getValue().toString();
                                                                                                      String money = dataSnapshot.child("money").getValue().toString();

                                                                                                      ImageView avatar = (ImageView) findViewById(R.id.avatarImageView);
                                                                                                      DataSnapshot ava = dataSnapshot.child("avatar");
                                                                                                      String avatarString = "";
                                                                                                      for(final DataSnapshot activeAvatar : ava.getChildren())
                                                                                                      {
                                                                                                          if(activeAvatar.getValue() == true){
                                                                                                              avatarString = activeAvatar.getKey().toString();
                                                                                                              break;
                                                                                                          }
                                                                                                      }
                                                                                                      int avaID = getResources().getIdentifier(avatarString,"drawable",getPackageName());
                                                                                                      avatar.setImageResource(avaID);

                                                                                                      TextView usernameText = (TextView) findViewById(R.id.usernameTextView);
                                                                                                      TextView rankText = (TextView) findViewById(R.id.rankTextView);
                                                                                                      TextView nextlevelText = (TextView) findViewById(R.id.nextLevelTextView);
                                                                                                      TextView moneyText = (TextView) findViewById(R.id.moneyTextView);

                                                                                                      usernameText.setText(name);
                                                                                                      rankText.setText(rank);
                                                                                                      nextlevelText.setText("Points : "+nextLevel);
                                                                                                      moneyText.setText(money);

                                                                                                      ((TextView) findViewById(R.id.goodday)).
                                                                                                              setText("What's up, " + name);

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
                                                                                                                  }
                                                                                                              }
                                                                                                          }
                                                                                                      } else {
                                                                                                          TextView text = new TextView(getApplicationContext());
                                                                                                          text.setText("You have no tasks today");
                                                                                                          ((LinearLayout) findViewById(R.id.tasks)).addView(text);
                                                                                                      }

                                                                                                      if (dataSnapshot.child("habits").exists()) {
                                                                                                          DataSnapshot habits = dataSnapshot.child("habits");

                                                                                                          for (final DataSnapshot type : habits.getChildren()) {

                                                                                                              for (final DataSnapshot habit : type.getChildren()) {
                                                                                                                  final Date now = Calendar.getInstance().getTime();
                                                                                                                  Date last = new Date(now.getYear() - 1, now.getMonth(), now.getDay());
                                                                                                                  final int span = type.getKey().equalsIgnoreCase("daily") ? 1 : type.getKey().equalsIgnoreCase("weekly") ? 7 : 30;

                                                                                                                  if (habit.hasChild("last")) {
                                                                                                                      try {
                                                                                                                          last = new SimpleDateFormat("dd-MM-yyyy").parse(habit.child("last").getValue().toString());
                                                                                                                      }catch (Exception e){

                                                                                                                      }
                                                                                                                  }

                                                                                                                  if (TimeUnit.DAYS.convert(now.getTime() - last.getTime(), TimeUnit.MILLISECONDS) > span) {


                                                                                                                      firebase.child("users/"
                                                                                                                              + firebase.getAuth().getUid()
                                                                                                                              + "/habits/"
                                                                                                                              + type.getKey()+"/"+habit.getKey()+"/done").setValue("false");


                                                                                                                      View deadline = LayoutInflater.from(getApplicationContext()).inflate(R.layout.deadline, null);

                                                                                                                      ((TextView) deadline.findViewWithTag("text")).setText(habit.getKey());
                                                                                                                      ((CheckBox) deadline.findViewWithTag("check")).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                                                                                                                                          + "/point").setValue(point+streak*10);


                                                                                                                                  buttonView.setEnabled(false);
                                                                                                                                  Toast.makeText(HomeActivity.this, "Habit finished!", Toast.LENGTH_SHORT).show();                                        }

                                                                                                                          }
                                                                                                                      });

                                                                                                                      ((LinearLayout) findViewById(R.id.tasks)).addView(deadline);
                                                                                                                  }

                                                                                                              }
                                                                                                          }
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
                                                                                                      }else{

                                                                                                          TextView text = new TextView(getApplicationContext());
                                                                                                          text.setText("You have no habits today");
                                                                                                          ((LinearLayout) findViewById(R.id.habits)).addView(text);
                                                                                                      }

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
                                                                                                  public void onCancelled (FirebaseError firebaseError){
                                                                                                      Toast.makeText(HomeActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                  }
                                                                                              }

        );


    }

    @Override
    public void onBackPressed () {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
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
    public boolean onNavigationItemSelected (MenuItem item){
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_edit_profile) {
            Toast.makeText(HomeActivity.this, "Opening edit profile..", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_shop) {
            Toast.makeText(HomeActivity.this, "Opening shop..", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), ShopActivity.class));
        } else if (id == R.id.nav_log_out) {
            firebase.unauth();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

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
