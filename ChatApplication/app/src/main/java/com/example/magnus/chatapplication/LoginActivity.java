package com.example.magnus.chatapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * LoginActivity class handled authentication of user and assigning username
 */

public class LoginActivity extends AppCompatActivity {

    public static final String AUTHENTICATION = "authentication";
    private static final String PREFS_NAME     = "MyPrefsFile";
    public static final String USERNAME     = "username";

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String mUsername;

    private FirebaseFirestore mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.mDatabase = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();

        handleUserName();
    }

    /**
     * Generate new username if username does not exist
     */
    private void handleUserName() {
        final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        this.mUsername = prefs.getString(USERNAME,"");

        if(this.mUsername.isEmpty()){

            TextView label = findViewById(R.id.user_userlabel);
            label.setVisibility(View.VISIBLE);
            TextView nameLabel = findViewById(R.id.user_label);
            nameLabel.setVisibility(View.INVISIBLE);
            EditText nameText = findViewById(R.id.user_write_username);
            nameText.setVisibility(View.VISIBLE);

           // Generate new userName
            this.mDatabase.collection("users")
                           // .whereEqualTo(username, true)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        int i = 0;
                                        String randomName = "Anonymous";
                                        Boolean exists;
                                        do {
                                            exists = false;
                                            for (DocumentSnapshot document : task.getResult()) {        // Check if username exist
                                                String dbName = (String)document.get("username");
                                                if (randomName.equalsIgnoreCase(dbName)) {
                                                    exists = true;
                                                }
                                            }

                                            if (exists){
                                                randomName = "Anonymous-" + i++;
                                            }

                                        } while(exists);

                                        EditText nameText = findViewById(R.id.user_write_username);
                                        nameText.setText(randomName);
                                    } else {

                                        Log.d("login", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
        } else {
            changeUIOnNameAccept(this.mUsername);
        }
    }


    /**
     * Accept username if not already in use,  when pressed, linked to OK button
     */
    public void checkUserName(@SuppressWarnings("unused") View view) {

        if(!((EditText)findViewById(R.id.user_write_username)).getText().toString().isEmpty()){
            // Check if user exist
            this.mDatabase.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
                                final SharedPreferences.Editor editor = prefs.edit();

                                String nameText = ((EditText)findViewById(R.id.user_write_username)).getText().toString();
                                Boolean exists = false;
                                for (DocumentSnapshot document : task.getResult()) {
                                    String dbName = (String) document.get("username");
                                    if(nameText.equalsIgnoreCase(dbName)){
                                        exists = true;
                                    }
                                }

                                if(exists){
                                    findViewById(R.id.user_invalid_label).setVisibility(View.VISIBLE);
                                    editor.putString(USERNAME, "");
                                    editor.apply();
                                } else {
                                    editor.putString(USERNAME, nameText);
                                    editor.apply();
                                    mUsername = nameText;

                                    changeUIOnNameAccept(nameText);
                                }

                            } else {

                                Log.d("login", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } else {
            Toast.makeText(this,"TextField empty",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Will change the ui if user name is accepted
     * @param nameText username
     */
    @SuppressLint("SetTextI18n")
    private void changeUIOnNameAccept(String nameText){
        findViewById(R.id.user_addbutton).setVisibility(View.INVISIBLE);
        TextView label = findViewById(R.id.user_userlabel);
        label.setVisibility(View.INVISIBLE);
        EditText name = findViewById(R.id.user_write_username);
        name.setVisibility(View.INVISIBLE);

        TextView userLabel = findViewById(R.id.user_label);
        userLabel.setVisibility(View.VISIBLE);
        userLabel.setText("Username: " + nameText); // Suppressed since we did not have to worry about I18N on labs
    }


    /**
     * signUp authenticates user with email and password handled by firebase
     * View warning is suppressed. needed for xml onClick to work
     */
    public void signUp(@SuppressWarnings("unused") View view){
        final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0); //TODO remove
        this.mUsername = prefs.getString(USERNAME,"");

        if(!this.mUsername.isEmpty()){
            String email = ((TextView)findViewById(R.id.login_email)).getText().toString();
            String password = ((TextView)findViewById(R.id.login_password)).getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("signUp", "createUserWithEmail:success");
                                mCurrentUser = mAuth.getCurrentUser();

                                addUsernameToDatabase();

                                sendAuthentication();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("signUp", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this,"Must accept user name",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Add username to db
     */
    private void addUsernameToDatabase() {
        if(mCurrentUser != null){
            Map<String,Object> user = new HashMap<>();
            user.put("username", mUsername);
            user.put("uid",mCurrentUser.getUid());

            mDatabase.collection("users")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("db", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("db", "Error adding document", e);
                        }
                    });

        }
    }

    /**
     * signIn sign in to current user with mail and password, handled by firebase
     * @param view need this to assign onClick in XML
     */
    public void signIn(@SuppressWarnings("unused") View view){
        final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        this.mUsername = prefs.getString(USERNAME,"");

        if(!this.mUsername.isEmpty()) {

            String email = ((TextView) findViewById(R.id.login_email)).getText().toString();
            String password = ((TextView) findViewById(R.id.login_password)).getText().toString();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("signIn", "signInWithEmail:success");
                                mCurrentUser = mAuth.getCurrentUser();
                                sendAuthentication();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("signIn", "signInWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        } else {
            Toast.makeText(this,"Must accept user name",Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Signs in anonymous user
     * View warning is suppressed. needed for xml onClick to work
     */
    public void anonymousSignIn(@SuppressWarnings("unused") View view){

        if(!this.mUsername.isEmpty()) {

            this.mDatabase.collection("users")
                    .whereEqualTo("username",mUsername)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                signInAnonymously();

                            } else {
                                Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_LONG).show();
                                Log.d("login", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } else {
            Toast.makeText(this,"Must accept user name",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Authenticate user, will keep same username if it already exist.
     * If not new username will be added to the database
     */
    private void signInAnonymously() {

        this.mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("anonymousSignIn", "signInAnonymously:success");
                            mCurrentUser = mAuth.getCurrentUser();

                            addUsernameToDatabase();
                            sendAuthentication();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("anonymousSignIn", "signInAnonymously:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Sends authentication to TabbedActivity
     */
    private void sendAuthentication(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(AUTHENTICATION,true);
        resultIntent.putExtra(USERNAME,this.mUsername);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Quit application when back press in login
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity(); // TODO permanent quit server
    }
}
