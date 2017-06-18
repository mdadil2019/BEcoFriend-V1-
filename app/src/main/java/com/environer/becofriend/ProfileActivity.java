package com.environer.becofriend;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.environer.becofriend.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int SELECT_PICTURE = 1;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private String profileImageLink;
    ProgressDialog progressDialog;
    @BindView(R.id.logOutBtn)Button logoutBtn;
    @BindView(R.id.textViewUploadImage)TextView uploadTv;
    @BindView(R.id.imageViewProfile)ImageView profileImageView;
    @BindView(R.id.editTextNameProfile)EditText profileName;
    @BindView(R.id.button_profileSet)Button profileSetBtn;
    private Uri imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        logoutBtn.setOnClickListener(this);
        uploadTv.setOnClickListener(this);
        profileImageView.setOnClickListener(this);
        profileSetBtn.setOnClickListener(this);
    }

    public void addUserInfo(String imageLink){
        String userName =  getIntent().getStringExtra("userName");
        mDatabase.child(Constants.USERS).child(userName).child(Constants.FULL_NAME).setValue(profileName.getText().toString());
        mDatabase.child(Constants.USERS).child(userName).child(Constants.IMAGE_LINK).setValue(imageLink);
    }

    public void pickImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent,"Select Picture"),SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_PICTURE && resultCode == RESULT_OK){
            Picasso.with(this).load(data.getData()).noPlaceholder().into(profileImageView, new Callback() {
                @Override
                public void onSuccess() {
                   imagePath = data.getData();
                }

                @Override
                public void onError() {

                }
            });
        }
    }

    private void uploadImageToFbStorage(Uri data) {
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();
        StorageReference imageRef = mStorageRef.child(Constants.PROFILE_IMAGES).child(String.valueOf(data));
        imageRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        profileImageLink = String.valueOf(taskSnapshot.getDownloadUrl());
                        if(getIntent().getExtras()!=null)
                            addUserInfo(profileImageLink);
                        progressDialog.dismiss();
                        startActivity(new Intent(ProfileActivity.this,ContentActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view == logoutBtn){
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if(firebaseAuth.getCurrentUser()!=null){
                firebaseAuth.signOut();
            }
        }
        else if(view == uploadTv || view == profileImageView){
            pickImage();
        }
        else if(view == profileSetBtn){
            if(profileImageLink!=null && !profileImageLink.equals("")){
                String pName = profileName.getText().toString();
                if(pName!=null && !pName.equals("")){
                        if(imagePath!=null && !imagePath.equals(""))
                            uploadImageToFbStorage(imagePath);

                }
            }
        }

    }
}
