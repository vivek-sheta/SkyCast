package com.example.skycast.ui.App;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skycast.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "VIVEK_AUTH"; // Custom log tag
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Log.d(TAG, "onCreate: Activity created");

        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");

            mAuth = FirebaseAuth.getInstance();
            Log.d(TAG, "FirebaseAuth instance obtained");

            // Configure Google Sign-In
            String clientId = getString(R.string.default_web_client_id);
            Log.d(TAG, "Using client ID: " + clientId);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d(TAG, "GoogleSignInClient configured");

            findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> {
                Log.d(TAG, "Google Sign-In button clicked");
                signIn();
            });

        } catch (Exception e) {
            Log.e(TAG, "Initialization error: " + e.getMessage(), e);
            Toast.makeText(this, "Initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void signIn() {
        try {
            Log.d(TAG, "Starting Google Sign-In flow");
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            Log.e(TAG, "SignIn error: " + e.getMessage(), e);
            Toast.makeText(this, "SignIn error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "Processing Google Sign-In result");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google Sign-In successful, account: " +
                        (account != null ? account.getEmail() : "null"));

                if (account != null && account.getIdToken() != null) {
                    Log.d(TAG, "ID Token received, proceeding with Firebase auth");
                    firebaseAuthWithGoogle(account);
                } else {
                    Log.e(TAG, "Google account or ID token is null");
                    Toast.makeText(this, "Google account information incomplete", Toast.LENGTH_LONG).show();
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google Sign-In failed: " + e.getStatusCode() + ", " + e.getMessage(), e);
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
                Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "Starting Firebase authentication with Google account");

        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            Log.d(TAG, "Firebase credential created");

            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        Log.d(TAG, "signInWithCredential onComplete: isSuccessful=" + task.isSuccessful());

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "Authentication successful. User: " +
                                    (user != null ? user.getEmail() : "null"));

                            startActivity(new Intent(AuthActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Exception exception = task.getException();
                            Log.e(TAG, "Authentication failed", exception);
                            Toast.makeText(AuthActivity.this,
                                    "Authentication failed: " + (exception != null ? exception.getMessage() : "Unknown error"),
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "signInWithCredential failed", e);
                        Toast.makeText(AuthActivity.this,
                                "Authentication failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error in firebaseAuthWithGoogle: " + e.getMessage(), e);
            Toast.makeText(this, "Authentication error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Checking current user");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already signed in: " + currentUser.getEmail());
            startActivity(new Intent(AuthActivity.this, MainActivity.class));
            finish();
        }
    }
}