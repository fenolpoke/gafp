package edu.bluejack151.gafp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class EditProfileActivity extends AppCompatActivity {

    Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

       Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");


    }

    public void editProfile(View view){
        Toast.makeText(EditProfileActivity.this, "Editing profile...", Toast.LENGTH_SHORT).show();
    }

}
