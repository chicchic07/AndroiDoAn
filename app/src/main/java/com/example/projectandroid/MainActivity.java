package com.example.projectandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Tag;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    //Login = Google
    private static final int RC_SIGN_IN=9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bắt đầu quá trình xác thực Google
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        Button login = findViewById(R.id.login);
        TextView signup = findViewById(R.id.signup);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user!=null) {
            Intent i = new Intent(MainActivity.this, Home.class);
            startActivity(i);
            finish();
        }

        login.setOnClickListener(view -> {
            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Thread thread = new Thread(() -> {
                String em = email.getText().toString();
                String pass = password.getText().toString();
                auth.signInWithEmailAndPassword(em, pass).addOnCompleteListener(MainActivity.this,
                        (OnCompleteListener<AuthResult>) task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user1 = auth.getCurrentUser();
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    Intent i = new Intent(MainActivity.this, Home.class);
                                    i.putExtra("User UID", user1.getUid());
                                    startActivity(i);
                                    finish();
                                });
                            } else {
                                Toast.makeText(MainActivity.this, "Operation Failed.", Toast.LENGTH_SHORT).show();
                                runOnUiThread(progressDialog::dismiss);
                            }
                        });
            });
            thread.start();
        });

        signup.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, Signup.class);
            startActivity(i);
            finish();
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Unexpect" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            HashMap<String,Object> map = new HashMap<>();
                            String[] name = user.getDisplayName().split(" ");
                            map.put("First Name", name[0]);
                            map.put("Last Name", name[1]);
                            DatabaseReference databaseFirebase = FirebaseDatabase.getInstance().getReference("Users");
                            databaseFirebase.child(user.getUid()).updateChildren(map);
                            Intent intent = new Intent(MainActivity.this, Home.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}