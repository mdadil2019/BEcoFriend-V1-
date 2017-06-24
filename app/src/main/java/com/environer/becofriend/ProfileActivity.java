package com.environer.becofriend;

import android.*;
import android.Manifest;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.environer.becofriend.utils.Constants;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
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

import static com.environer.becofriend.utils.Constants.CITY;
import static com.environer.becofriend.utils.Constants.FINE_LOCATION;
import static com.environer.becofriend.utils.Constants.FULL_NAME;
import static com.environer.becofriend.utils.Constants.MAINUSER_IMAGELINK;
import static com.environer.becofriend.utils.Constants.WRITE_PERMISSION;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int SELECT_PICTURE = 1;
    private static final int CITY_SELECT_REQUEST = 2;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private String profileImageLink;
    ProgressDialog progressDialog;
    @BindView(R.id.logOutBtn)Button logoutBtn;
    @BindView(R.id.textViewUploadImage)TextView uploadTv;
    @BindView(R.id.imageViewProfile)ImageView profileImageView;
    @BindView(R.id.editTextNameProfile)EditText profileName;
    @BindView(R.id.button_profileSet)Button profileSetBtn;
    @BindView(R.id.textViewSelectCity)TextView selectCity;
    private Uri imagePath;
    private String selectedCity;

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
        selectCity.setOnClickListener(this);

        autoUpdateProfile();
    }

    private void autoUpdateProfile() {
        if(getIntent().getStringExtra(MAINUSER_IMAGELINK)!=null){
            Picasso.with(this).load(getIntent().getStringExtra(MAINUSER_IMAGELINK)).error(R.drawable.error).into(profileImageView);
            selectCity.setText(getIntent().getStringExtra(CITY));
            profileName.setText(getIntent().getStringExtra(FULL_NAME));
        }
    }

    public void addUserInfo(String imageLink){
//        String city = spinner.getSelectedItem().toString();
            String city = selectedCity.replace(',',' ');//Since firebase wouldn't take keys with special characters
        String userName =  getIntent().getStringExtra("userName");
        mDatabase.child(Constants.USERS).child(userName).child(Constants.FULL_NAME).setValue(profileName.getText().toString());
        mDatabase.child(Constants.USERS).child(userName).child(MAINUSER_IMAGELINK).setValue(imageLink);
        mDatabase.child(Constants.USERS).child(userName).child(Constants.CITY).setValue(city);
        saveData(userName,city);
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
        else if(requestCode == CITY_SELECT_REQUEST){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(this,data);
                selectedCity = String.valueOf(place.getAddress());
                selectCity.setText(selectedCity);
                Toast.makeText(this, "Make sure you have selected your city by searching from search box", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void getDataAccessPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.EXTERNAL_READ_REQUEST);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
        }
    }



    private void getLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},Constants.LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION);
        }
    }

    private void uploadImageToFbStorage(Uri data) {
        progressDialog.setMessage("Uploading your details...");
        progressDialog.show();
        StorageReference imageRef = mStorageRef.child(Constants.PROFILE_IMAGES).child(String.valueOf(data));
        imageRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //noinspection VisibleForTests
                        profileImageLink = String.valueOf(taskSnapshot.getDownloadUrl());
                        if(getIntent().getExtras()!=null)
                            addUserInfo(profileImageLink);
                        progressDialog.dismiss();
                        //Setting animation
                        if(Build.VERSION.SDK_INT>= 21)
                        {
                            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(ProfileActivity.this).toBundle();
                            startActivity(new Intent(ProfileActivity.this,ContentActivity.class),bundle);
                        }
                        else
                            startActivity(new Intent(ProfileActivity.this,ContentActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveData(String userName,String city) {
        SharedPreferences sharedPreferences = getSharedPreferences("Shrd",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.USER_NAME,userName);
        editor.putString(Constants.CITY,city);
        editor.apply();
    }

    @Override
    public void onClick(View view) {
        if(view == logoutBtn){
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if(firebaseAuth.getCurrentUser()!=null){
                firebaseAuth.signOut();
                ContentActivity.contentAct.finish();
                startActivity(new Intent(ProfileActivity.this,LoginActivity.class));
                finish();
            }
        }
        else if(view == uploadTv || view == profileImageView){
            getDataAccessPermission();
            pickImage();
        }
        else if(view == selectCity){
            getLocationPermission();
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this),CITY_SELECT_REQUEST);
                Toast.makeText(this, "Search your city name in above search box", Toast.LENGTH_LONG).show();
            } catch (GooglePlayServicesRepairableException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (GooglePlayServicesNotAvailableException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else if(view == profileSetBtn){
                String pName = profileName.getText().toString();
                if(pName!=null && !pName.equals("")){
                        if(imagePath!=null && !imagePath.equals("")) {
                            if(!selectedCity.equals("Select City")&&!selectedCity.equals("")){
                                //call uploadImageToFbStorage
                                uploadImageToFbStorage(imagePath);
                            }
                        }

                }

        }

    }
}
