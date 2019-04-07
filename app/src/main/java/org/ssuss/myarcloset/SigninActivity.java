package org.ssuss.myarcloset;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SigninActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 11;

    private static final String TAG = "log :: ";

    private String address = "@myarcloset.org";

    //UI
    private View view;
    private EditText IDView;
    private EditText passwordView;
    private Button IDSigninBtn;
    private SignInButton googleSigninBtn;

    private String id;
    private String password;

    //firebase Authentication
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        //init firebase instance
        mAuth = FirebaseAuth.getInstance();

        //UI
        view = findViewById(R.id.layout);
        IDView = (EditText) findViewById(R.id.editText_email);
        passwordView = (EditText) findViewById(R.id.editText_pw);
        IDSigninBtn = (Button) findViewById(R.id.email_sign_in_button);
        googleSigninBtn = (SignInButton) findViewById(R.id.google_sign_in_button);


        IDSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = IDView.getText().toString();
                password = passwordView.getText().toString();
                System.out.println("**id : "+id+"\n**password :"+password);
                signInEvent(id, password);
            }
        });

        //login interface listener
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user!=null){
                    //login
                    Intent intent = new Intent(SigninActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //logout
                }

            }
        };

        //Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("340012457649-0nbfonrbtfmngmodk1p6vr300a6hr5b3.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSigninBtn = (SignInButton) findViewById(R.id.google_sign_in_button);
        googleSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }
    /**
     * end of onCreate()
     **/

    private void signInEvent(final String id, final String pw){
        boolean isCanceled = attemptLogin();
        if(isCanceled == true){
            Toast.makeText(SigninActivity.this, "E-mail과 Password를 확인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            mAuth.signInWithEmailAndPassword(id+address, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        //login failed
                        Log.d(TAG, task.getException().getMessage());
                        Toast.makeText(SigninActivity.this, "등록되지 않은 회원입니다.", Toast.LENGTH_SHORT).show();
                        final Snackbar s = Snackbar.make(view, "계정이 새로 생성됩니다.", Snackbar.LENGTH_INDEFINITE);
                        s.setAction("YES", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                createUser(id+address, pw);
                                s.dismiss();
                            }
                        }).show();
                    } else {
                        //login success
                        Toast.makeText(SigninActivity.this, "패션피플님 환영합니다 :)", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(authStateListener); // ******************
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }

    private void updateUI(FirebaseUser user) {
//        hideProgressDialog();
        if (user != null) {
//            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
//            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.google_sign_in_button).setVisibility(View.GONE);
//            findViewById(R.id.signOutAndDisconnect).setVisibility(View.VISIBLE);
        } else {
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);

            findViewById(R.id.google_sign_in_button).setVisibility(View.VISIBLE);
//            findViewById(R.id.signOutAndDisconnect).setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                System.out.println("**구글 로그인 성공 onActivityResult");
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                System.out.println("**구글 로그인 실패 onActivityResult");

                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            System.out.println("**구글 로그인 성공 firebaseAuthWithGoogle");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                            System.out.println("**구글 로그인 실패 firebaseAuthWithGoogle");

                        }

                        // ...
                    }
                });
    }


    private void createUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithID:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            System.out.println("** id login // createUser 성공!");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithID:failure", task.getException());
                            updateUI(null);
                            System.out.println("** id login // createUser 실패!");

                        }

                        // ...
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //if failed...
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid id, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private boolean attemptLogin() {

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid id .
        if (TextUtils.isEmpty(id)) {
            IDView.setError(getString(R.string.error_field_required));
            focusView = IDView;
            cancel = true;
        } else if (!isIDValid(id)) {
            IDView.setError(getString(R.string.error_invalid_id));
            focusView = IDView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }

        return cancel;
    }

    private boolean isIDValid(String id) {
        //TODO: Replace this with your own logic
        //return email.contains("@");
        return id.length() >= 4;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 6;
    }
}
