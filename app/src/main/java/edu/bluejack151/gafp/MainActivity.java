package edu.bluejack151.gafp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Firebase firebase;
    CallbackManager callbackManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
//        loginManager = LoginManager.getInstance();
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");
        LoginButton facebookButton = (LoginButton) findViewById(R.id.facebook_button);

        facebookButton.setReadPermissions("email");

        if (!checkFacebookUser()) {
            LoginManager.getInstance().logOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }


        firebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    Toast.makeText(MainActivity.this, "Succesfully logged in!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                }
//
//              else{
//                    Toast.makeText(MainActivity.this, "Failed to log in!", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {

                Toast.makeText(MainActivity.this, "Login with facebook succeeded!", Toast.LENGTH_SHORT).show();

                if(!checkFacebookUser()){
                    Toast.makeText(MainActivity.this, "Creating user..", Toast.LENGTH_SHORT).show();

                    GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(final JSONObject object, GraphResponse response) {


                            Toast.makeText(MainActivity.this, "Mapping user..", Toast.LENGTH_SHORT).show();

                            String email = null;

                            try {
                                email = object.getString("email");
                            } catch (Exception e) {
                                Log.e("getting email", e.getMessage());
                                email = loginResult.getAccessToken().getUserId() + "@yahoo.com";
                            }


                            final String finalEmail = email;
                            firebase.createUser(email, "", new Firebase.ResultHandler() {
                                @Override
                                public void onSuccess() {
                                    try {
                                        Toast.makeText(MainActivity.this, "Authenticating...", Toast.LENGTH_SHORT).show();
                                        firebase.authWithPassword(object.getString("email"), "", new Firebase.AuthResultHandler() {
                                            @Override
                                            public void onAuthenticated(AuthData authData) {
                                                if (authData != null) {


                                                    Map<String, Object> def = new HashMap<String, Object>();
                                                    def.put("default", true);

                                                    Map<String, Object> values = new HashMap<String, Object>();
                                                    values.put("username", Profile.getCurrentProfile().getName());
                                                    values.put("themes", def);
                                                    values.put("avatar", def);
                                                    values.put("money", 0);
                                                    values.put("rank", "amateur");
                                                    values.put("point", 0);
                                                    values.put("password", "");
                                                    values.put("email", finalEmail);

                                                    Map<String, Object> user = new HashMap<String, Object>();
                                                    user.put(authData.getUid(), values);

                                                    firebase.child("users").updateChildren(user);


                                                    Toast.makeText(MainActivity.this, "Login " + Profile.getCurrentProfile().getName() + " Success!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                                } else {
                                                    Toast.makeText(MainActivity.this, "Error logining", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onAuthenticationError(FirebaseError firebaseError) {
                                                Toast.makeText(MainActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onError(FirebaseError firebaseError) {

                                    Toast.makeText(MainActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });



                        }
                    });

                    Bundle parameters = new Bundle();
//                            parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location");
                    parameters.putString("fields", "email");
                    graphRequest.setParameters(parameters);
                    graphRequest.executeAsync();

                }

            }


            @Override
            public void onCancel() {

                Toast.makeText(MainActivity.this, "cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {

                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

//        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                Toast.makeText(MainActivity.this, "Login manager login suceeded!", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
//            }
//
//            @Override
//            public void onCancel() {
//                Toast.makeText(MainActivity.this, "cancelled!", Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//
//            }
//        });

    }

    boolean checkFacebookUser(){

        if (Profile.getCurrentProfile() != null) return false;

        firebase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    if (user.hasChild("facebook") && user.child("facebook").getValue().toString() == Profile.getCurrentProfile().getId()) {
                        firebase.authWithPassword(user.child("email").getValue().toString(), user.child("password").getValue().toString(), new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(AuthData authData) {
                                if (authData != null) {
                                    Toast.makeText(MainActivity.this, "Facebook login authenticated!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));

                                }
                            }

                            @Override
                            public void onAuthenticationError(FirebaseError firebaseError) {

                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return false;
    }
    public void login(View view) {
        Toast.makeText(MainActivity.this, "Loggin in...", Toast.LENGTH_SHORT).show();

        EditText email = (EditText) findViewById(R.id.emailEditText);
        EditText password = (EditText) findViewById(R.id.passwordEditText);


        if (email.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Email must be filled!", Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            Toast.makeText(MainActivity.this, "Email must match email format!", Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return;
        } else if (password.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Password must be filled!", Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return;
        }

//        TableRow.LayoutParams trlp = new TableRow.LayoutParams();
//        trlp.width = trlp.height = trlp.MATCH_PARENT;
//
//        TableRow tr = new TableRow(this);
//
//        tr.setLayoutParams(trlp);

        Toast.makeText(MainActivity.this, "Authenticating...", Toast.LENGTH_SHORT).show();

        firebase.authWithPassword(
                email.getText().toString(),
                password.getText().toString(),
                new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        if (authData != null) {
                            Toast.makeText(MainActivity.this, "Authenticated!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        }
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Toast.makeText(MainActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void openRegister(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

}
