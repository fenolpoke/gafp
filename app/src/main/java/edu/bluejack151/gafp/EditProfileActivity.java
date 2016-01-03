package edu.bluejack151.gafp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

       Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

        firebase.child("users/"+firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<String> avatars = new ArrayList<String>();
                ArrayList<String> themes = new ArrayList<String>();
                int posAvatar = 0;
                int posTheme = 0;

                for(DataSnapshot avatar : dataSnapshot.child("avatar").getChildren()){
                    avatars.add(avatar.getKey());
                    if(!avatar.getValue().toString().equalsIgnoreCase("true")){
                        posAvatar++;
                    }
                }

                for(DataSnapshot theme : dataSnapshot.child("themes").getChildren()){
                    themes.add(theme.getKey());
                    if(!theme.getValue().toString().equalsIgnoreCase("true")){
                        posTheme++;
                    }
                }

                if (avatars.isEmpty()){
                    avatars.add("default");
                }
                if(themes.isEmpty()){
                    themes.add("default");
                }
                ArrayAdapter<String> adapterAvatar =
                        new ArrayAdapter<String>(
                                getApplicationContext(),
                                R.layout.support_simple_spinner_dropdown_item,
                                avatars
                        );

                ArrayAdapter<String> adapterTheme =
                        new ArrayAdapter<String>(
                                getApplicationContext(),
                                R.layout.support_simple_spinner_dropdown_item,
                                themes
                        );

                Spinner avatarSpinner = (Spinner) findViewById(R.id.avatarSpinner);
                Spinner themeSpinner = (Spinner) findViewById(R.id.themeSpinner);

                avatarSpinner.setAdapter(adapterAvatar);
                avatarSpinner.setSelection(posAvatar);
                themeSpinner.setAdapter(adapterTheme);
                themeSpinner.setSelection(posTheme);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });




    }

    public void editProfile(View view){
        Toast.makeText(EditProfileActivity.this, "Editing profile...", Toast.LENGTH_SHORT).show();

        final EditText password = (EditText) findViewById(R.id.passwordEditText);
        EditText confirm = (EditText) findViewById(R.id.confirmPasswordEditText);
        final EditText username = (EditText) findViewById(R.id.usernameEditText);


        if(username.getText().toString().isEmpty()){
            Toast.makeText(EditProfileActivity.this, "Username must be filled!", Toast.LENGTH_SHORT).show();
            username.requestFocus();
            return;
        }else if(password.getText().toString().isEmpty()){
            Toast.makeText(EditProfileActivity.this, "Password must be filled!", Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return;
        }else if(!password.getText().toString().equals(confirm.getText().toString())){
            Toast.makeText(EditProfileActivity.this, "Password and confirm must be the same!", Toast.LENGTH_SHORT).show();
            confirm.requestFocus();
            return;
        }else{


            firebase.child("users/"+firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Spinner sp = (Spinner)findViewById(R.id.avatarSpinner);

                    Map<String, Object> avatars = new HashMap<String, Object>();
                    Map<String, Object> themes = new HashMap<String, Object>();

                    for (DataSnapshot avatar : dataSnapshot.child("avatar").getChildren()) {
                        avatars.put(avatar.getKey(), (sp.getSelectedItem().toString() == avatar.getKey()) ? true : false);
                    }
                    for (DataSnapshot theme : dataSnapshot.child("themes").getChildren()) {
                        themes.put(theme.getKey(), (sp.getSelectedItem().toString() == theme.getKey()) ? true : false);
                    }


                    Map<String, Object> user = new HashMap<String, Object>();
                    user.put("username", username.getText().toString());
                    user.put("password", password.getText().toString());
                    user.put("avatar", avatars);
                    user.put("themes", themes);

                    firebase.changePassword(
                            dataSnapshot.child("email").getValue().toString(),
                            dataSnapshot.child("password").getValue().toString(),
                            password.getText().toString(),
                            new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(EditProfileActivity.this, "Change password success!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Toast.makeText(EditProfileActivity.this, "change fail: "+firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    firebase.child("users/"+firebase.getAuth().getUid()).updateChildren(user);

                    Toast.makeText(EditProfileActivity.this, "Edit profile success!", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                    finish();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });


        }


    }

}
