package com.environer.becofriend;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private boolean uniqueness;
    private ProgressDialog progressDialog;
    @BindView(R.id.googleSignInBtn)SignInButton googleBtn;
    @BindView(R.id.editTextEmailLog)EditText emailEditText;
    @BindView(R.id.editTextPassLog)EditText passwordEditText;
    @BindView(R.id.signinBtn)Button signInBtn;
    @BindView(R.id.textViewNewUser)TextView newUserView;
    private boolean created;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        configureGoogleSignIn();
        googleBtn.setOnClickListener(this);
        signInBtn.setOnClickListener(this);
        newUserView.setOnClickListener(this);
    }

    public void setupGoogleApiClient(GoogleSignInOptions gs){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,gs)
                .build();
    }
    public void configureGoogleSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        setupGoogleApiClient(gso);
    }
    private void signIn() {
        
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(LoginActivity.this,ProfileActivity.class));
                            Toast.makeText(LoginActivity.this,"Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        if(view == googleBtn){
            signIn();
        }
        else if(view == signInBtn){
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if(!email.equals("") && !password.equals(""))
                signInWithEmail(email,password);
        }
        else if(view == newUserView){
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.registration_dialog);
            dialog.show();
            final EditText newUserName = (EditText)dialog.findViewById(R.id.editTextUserNameReg);
            final EditText password = (EditText)dialog.findViewById(R.id.editTextPassReg);
            Button signUpBtn = (Button)dialog.findViewById(R.id.button_signUp);


            signUpBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    final String userName = newUserName.getText().toString();
                    final String pass = password.getText().toString();
                    //Check that the user name is already exist or not in databse
                    //Show status with spinning bar
                    //If not exist then register it
                    if(!userName.equals("") && !pass.equals("")) {
                        mDatabase.child("Users").child(userName).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    createId(userName,pass);
                                }
                                else{
                                    if(!created)
                                        Toast.makeText(LoginActivity.this, "Username already exist, Please chose another", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }else{
                        Toast.makeText(LoginActivity.this, "Please enter the credentials!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void createId(final String userName, String pass) {
        created = true;
        String email = userName+"@eco.com";
        progressDialog.setMessage("Creating your account...");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    progressDialog.dismiss();
                    Intent i = new Intent(LoginActivity.this,ProfileActivity.class);
                    i.putExtra("userName",userName);
                    startActivity(i);
                    Toast.makeText(LoginActivity.this, "Welcome " + userName, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithEmail(String email, String password) {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
