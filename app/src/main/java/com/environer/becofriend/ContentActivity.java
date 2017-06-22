package com.environer.becofriend;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.environer.becofriend.adapter.ContentAdapter;
import com.environer.becofriend.data.FetchAllData;
import com.environer.becofriend.data.FetchCityData;
import com.environer.becofriend.utils.Constants;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;

import static com.environer.becofriend.utils.Constants.*;
import static com.environer.becofriend.utils.Constants.CITY;
import static com.environer.becofriend.utils.Constants.MAIN_USER;
import static com.environer.becofriend.utils.Constants.POST_IMAGE;
import static com.environer.becofriend.utils.Constants.USER_NAME;

public class ContentActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST = 1;
    private static final int PICKER_REQUEST = 2;
    private static final int FINE_LCN_PERMISSION = 3;
    private static final int COARSE_LCN_PERMISSION = 4;
    private static final int VIDEO_REQUEST = 5;
    private int CAMERA_PERMISSION   =11;
    private int EXTERNAL_READ_REQUEST = 12 ;
    private int  WRITE_PERMISSION = 13;

    private File contentDirectory;
    private File imageFile;
    private File videoFile;
    private String address,coOrdinate;
    private boolean isImage;
    private ProgressDialog progressDialog;
    public static Activity contentAct;
    DatabaseReference mDatabase;
    StorageReference mStorage;
    @BindView(R.id.fab_menu)FloatingActionButton menu_fab;
    private TextView selectLocation;
    @BindView(R.id.recyclerView)RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        contentAct = this;
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        menu_fab.setOnClickListener(this);
//        FetchCityData myCityData = new FetchCityData(this,recyclerView);
//        myCityData.getData();
        FetchAllData fetchAllData = new FetchAllData(this,recyclerView);
        fetchAllData.getAllData();

    }

    @Override
    public void onClick(View view) {
        if(view == menu_fab){
            final Dialog menu_dialog = new Dialog(this);
            menu_dialog.setContentView(R.layout.dialog_menu);
            menu_dialog.show();
            TextView takeImageTv = (TextView) menu_dialog.findViewById(R.id.textViewTakeImage);
            TextView takeVideoTv = (TextView) menu_dialog.findViewById(R.id.textViewTakeVideo);
            TextView setProfileTv = (TextView) menu_dialog.findViewById(R.id.textViewProfileMenu);
            takeImageTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getPermissions();
                    menu_dialog.dismiss();
                    takeImage();
                }
            });
            takeVideoTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getPermissions();
                    menu_dialog.dismiss();
                    takeVideo();
                }
            });
            setProfileTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu_dialog.dismiss();
                    getInfoAndStartIntent();
                }
            });
        }
    }

    private void getInfoAndStartIntent() {
        String[] info = getUserInfo();
        final String userName = info[1];
        final String city = info[0];

        mDatabase.child(USERS).child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fullName = dataSnapshot.child(FULL_NAME).getValue().toString();
                String imageLink = dataSnapshot.child(MAINUSER_IMAGELINK).getValue().toString();
                Intent intent = new Intent(ContentActivity.this,ProfileActivity.class);
                intent.putExtra(USER_NAME,userName);
                intent.putExtra(CITY,city);
                intent.putExtra(FULL_NAME,fullName);
                intent.putExtra(MAINUSER_IMAGELINK,imageLink);
                startActivity(intent);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void takeVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        contentDirectory = new File(Environment.getExternalStorageDirectory(), CONTENT_DIRECTORY_NAME);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        videoFile = new File(contentDirectory.getPath(),"VID_" + timeStamp + ".3gp");
        if(!contentDirectory.exists())
            contentDirectory.mkdir();
        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(videoFile));
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
        startActivityForResult(intent,VIDEO_REQUEST);

    }

    void getPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},EXTERNAL_READ_REQUEST);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},COARSE_LCN_PERMISSION);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_LCN_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERMISSION && grantResults[0] == RESULT_CANCELED){
            Toast.makeText(this,"You can't post the images!!. It requires permission",Toast.LENGTH_LONG).show();
        }
        //How to retrieve the index of my location permission for grantResult in order to check it??

    }

    @Override
    protected void onPause() {
        super.onPause();
        SimpleExoPlayer  exo = ContentAdapter.returnInstance();
        if(exo!=null){
            exo.release();
            exo .stop();
            exo = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SimpleExoPlayer  exo = ContentAdapter.returnInstance();
        if(exo!=null){
            exo.release();
            exo .stop();
            exo = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            if(imageFile!=null){
              //show information dialog
                isImage = true;
                askUserAboutIssue();
            }
        }
        else if(requestCode == PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(this,data);
                address = String.valueOf(place.getAddress());
                coOrdinate = String.valueOf(place.getLatLng());
                selectLocation.setText(address);

            }
            else{
                Toast.makeText(this, "Please select the place!!", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == VIDEO_REQUEST && resultCode == RESULT_OK){
            if(videoFile !=null){
                isImage = false;
                askUserAboutIssue();
            }
        }
    }

    private void askUserAboutIssue() {
        final Dialog infoDialog = new Dialog(this);
        infoDialog.setContentView(R.layout.dialog_info);
        infoDialog.show();
        final EditText problemEt = (EditText)infoDialog.findViewById(R.id.editTextInfo);
        selectLocation = (TextView)infoDialog.findViewById(R.id.textViewLocation);
        Button postBtn =(Button)infoDialog.findViewById(R.id.button_post);
        selectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launch Place Picker Intent
                getPermissions();
                placePicker();
            }
        });
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Upload the image to firebase databse
                //Upload the database of this post
                String problem = problemEt.getText().toString();
                if(problem!=null) {
                    infoDialog.dismiss();
                    UploadAllInfo(problem);
                }
                else{
                    Toast.makeText(ContentActivity.this, "Please write your problem!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void UploadAllInfo(final String problem) {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        StorageReference ref;
        File fileToUpload=null;
        if(isImage) {
            ref = mStorage.child(POST_IMAGE).child(imageFile.getName());
//            fileToUpload = imageFile;
            try {
                fileToUpload = new Compressor(this).compressToFile(imageFile);
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            ref = mStorage.child(POST_VIDEO).child(videoFile.getName());
            fileToUpload = videoFile;

        }



            ref.putFile(Uri.fromFile(fileToUpload)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadLink = String.valueOf(taskSnapshot.getDownloadUrl());
                    storeData(problem,downloadLink,progressDialog);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ContentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    }

    public String[] getUserInfo(){
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCES,MODE_PRIVATE);
        String[] info = new String[2];
        info[0] = sharedPref.getString(CITY,"Other");
        info[1] = sharedPref.getString(USER_NAME,"SomeOne");
        return info;
    }
    private void storeData(String problem, String downloadLink, ProgressDialog progressDialog) {
        String[] user_info = getUserInfo();//retrieve city and username from Shared preferences
        String city = user_info[0];
        String username = user_info[1];
        String latitude = coOrdinate.substring(coOrdinate.indexOf('(')+1,coOrdinate.indexOf(',')-1);
        String longitude = coOrdinate.substring(coOrdinate.indexOf(',')+1,coOrdinate.length()-1);
        DatabaseReference cityDb = mDatabase.child(CITY).child(city);

        DatabaseReference postDb = cityDb.push();
        postDb.child(PROBLEM).setValue(problem);
        postDb.child(LATITUDE).setValue(latitude);
        postDb.child(LONGITUDE).setValue(longitude);
        postDb.child(ADDRESS).setValue(address);
        postDb.child(RATING).child(TOTAL_RATING).setValue("5");
        if(isImage)
            postDb.child(POST_IMAGE).setValue(downloadLink);
        else
            postDb.child(POST_VIDEO).setValue(downloadLink);
        //add city name so need to store from profile into preference and will retrieved here
        //do same for user name, full name and profile picture download link.
        postDb.child(MAIN_USER).setValue(username);
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void placePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this),PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void takeImage() {
        //Be modular
        //Launch Camera Intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        contentDirectory = new File(Environment.getExternalStorageDirectory(), CONTENT_DIRECTORY_NAME);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        imageFile = new File(contentDirectory.getPath(),"IMG_" + timeStamp + ".jpg");
        if(!contentDirectory.exists())
            contentDirectory.mkdir();

        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(imageFile));
        startActivityForResult(intent,CAMERA_REQUEST);
        //onSucess upload it to firebase storage
        //onSuccess uplaoding, get the download link and store in a variable
        //create users problem asking dialog
        //Use Places Picker to select location and retrieve the city name from there
        //When imageLink,Problem, Location coordinates,City name are reterived then push data into respetive child


    }
}
