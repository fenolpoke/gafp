package edu.bluejack151.gafp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

        ((EditText)findViewById(R.id.usernameEditText)).requestFocus();
    }

    public void register(View view){
        Toast.makeText(RegisterActivity.this, "Registering...", Toast.LENGTH_SHORT).show();

        final EditText email = (EditText) findViewById(R.id.emailEditText);
        final EditText password = (EditText) findViewById(R.id.passwordEditText);
        EditText confirm = (EditText) findViewById(R.id.confirmPasswordEditText);
        final EditText username = (EditText) findViewById(R.id.usernameEditText);


        if(username.getText().toString().isEmpty()){
            Toast.makeText(RegisterActivity.this, "Username must be filled!", Toast.LENGTH_SHORT).show();
            username.requestFocus();
            return;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            Toast.makeText(RegisterActivity.this, "Email must match email format!", Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return;
        }else if(email.getText().toString().isEmpty()){
            Toast.makeText(RegisterActivity.this, "Email must be filled!", Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return;
        }else if(password.getText().toString().isEmpty()){
            Toast.makeText(RegisterActivity.this, "Password must be filled!", Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return;
        }else if(!password.getText().toString().equals(confirm.getText().toString())){
            Toast.makeText(RegisterActivity.this, "Password and confirm must be the same!", Toast.LENGTH_SHORT).show();
            confirm.requestFocus();
            return;
        }


        firebase.createUser(email.getText().toString(), password.getText().toString(), new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                Toast.makeText(RegisterActivity.this, "Register success!", Toast.LENGTH_SHORT).show();

                firebase.authWithPassword(email.getText().toString(), password.getText().toString(), new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        if (authData == null) {
                            Toast.makeText(RegisterActivity.this, "Can't login", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> def = new HashMap<String, Object>();
                            def.put("default", true);

                            Map<String, Object> values = new HashMap<String, Object>();
                            values.put("username", username.getText().toString());
                            values.put("themes", def);
                            values.put("avatar", def);
                            values.put("money", 0);
                            values.put("rank", "amateur");
                            values.put("point", 0);
                            values.put("email", email.getText().toString());
                            values.put("password", password.getText().toString());

                            Map<String, Object> user = new HashMap<String, Object>();
                            user.put(authData.getUid(), values);

                            firebase.child("users").updateChildren(user);

                            Toast.makeText(RegisterActivity.this, "Login " + authData.getUid() + " Success!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        }
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Toast.makeText(RegisterActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }

            @Override
            public void onError(FirebaseError firebaseError) {
                Toast.makeText(RegisterActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

//        Toast.makeText(RegisterActivity.this, "Register succesful!", Toast.LENGTH_SHORT).show();
        
    }
}
