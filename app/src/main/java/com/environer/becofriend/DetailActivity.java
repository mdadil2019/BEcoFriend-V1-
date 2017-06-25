package com.environer.becofriend;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.environer.becofriend.utils.Constants;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.environer.becofriend.utils.Constants.ADDRESS;
import static com.environer.becofriend.utils.Constants.FULL_NAME;
import static com.environer.becofriend.utils.Constants.LATITUDE;
import static com.environer.becofriend.utils.Constants.LONGITUDE;
import static com.environer.becofriend.utils.Constants.MAINUSER_IMAGELINK;
import static com.environer.becofriend.utils.Constants.MAIN_USER;
import static com.environer.becofriend.utils.Constants.MESSAGE;
import static com.environer.becofriend.utils.Constants.POST_IMAGE;
import static com.environer.becofriend.utils.Constants.POST_VIDEO;
import static com.environer.becofriend.utils.Constants.PROBLEM;
import static com.environer.becofriend.utils.Constants.RATING;
import static com.environer.becofriend.utils.Constants.USERS;

public class DetailActivity extends AppCompatActivity {

    SimpleExoPlayer mExoPlayer;
    DatabaseReference mDatabase;
    String problem,address,rating,mainUser,imageLink,videoLink, mainUserImageLink, mainUserFullName,latitude,longitude;
    boolean isImage;
    @BindView(R.id.imageViewDetail)ImageView imageView;
    @BindView(R.id.exoPlayerViewDetail)SimpleExoPlayerView exoPlayerView;
    @BindView(R.id.textViewProblemDetail)TextView problemTv;
    @BindView(R.id.textViewAddressDetail)TextView addressTv;
    @BindView(R.id.textViewRatingLabelDetail)TextView ratingTv;
    @BindView(R.id.imageViewMapDetail)ImageView imageViewMap;
    @BindView(R.id.circularImgMainUser)CircleImageView mainUserImage;
    @BindView(R.id.textViewMainUDetail)TextView mainUserName;
    @BindView(R.id.fabEmail)FloatingActionButton mailBtn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        checkIncomingData();
        imageViewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMapIntent();
            }
        });
        mailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchEmailIntent();
            }
        });
    }




    private void launchMapIntent() {
        double lat = Double.valueOf(latitude);
        double longi = Double.valueOf(longitude);
        String label = "BEcoFriend";
        String uriBegin = "geo:" + lat + "," + longi;
        String query = lat + "," + longi + "(" + label + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);

    }

    private void launchEmailIntent() {
        String contentLink;
        if(isImage)
            contentLink = imageLink;
        else
            contentLink = videoLink;

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","type_here@gmail.com",null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Serious issue noticed by Environer.Inc");
        emailIntent.putExtra(Intent.EXTRA_TEXT,MESSAGE+"\n" + PROBLEM  + ": " + problem + "\n"
                + "Content link: " + contentLink + "\n" +
                         ADDRESS + " : " +
                        address + "\n"+
                "Reported by: " + mainUserFullName);
        startActivity(Intent.createChooser(emailIntent,"Send email..."));
    }

    private void checkIncomingData() {
        progressDialog.setMessage("Retrieving Details...");
        progressDialog.show();
        if(getIntent().getExtras()!=null){
            problem = getIntent().getStringExtra(PROBLEM);
            address = getIntent().getStringExtra(ADDRESS);
            rating = getIntent().getStringExtra(RATING);
            mainUser = getIntent().getStringExtra(MAIN_USER);
            latitude = getIntent().getStringExtra(LATITUDE);
            longitude = getIntent().getStringExtra(LONGITUDE);

            if(getIntent().getStringExtra(POST_IMAGE)!=null) {
                imageLink = getIntent().getStringExtra(POST_IMAGE);
                isImage = true;
            }
            else {
                videoLink = getIntent().getStringExtra(POST_VIDEO);
                isImage = false;
            }

            getMainUserData(mainUser);
        }
    }

    private void getMainUserData(String mainUser) {
        mDatabase.child(USERS).child(mainUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mainUserImageLink = dataSnapshot.child(MAINUSER_IMAGELINK).getValue().toString();
                    mainUserFullName = dataSnapshot.child(FULL_NAME).getValue().toString();
                    updateInfoToLayout(mainUserImageLink,mainUserFullName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mExoPlayer!=null){
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(progressDialog.isShowing())
            progressDialog.dismiss();

        if(mExoPlayer!=null){
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    private void updateInfoToLayout(String mainUserImageLink, String mainUserFullName) {
        if(isImage){
            exoPlayerView.setVisibility(View.GONE);
            Picasso.with(this).load(imageLink).error(R.drawable.error).into(imageView);
        }
        else{
            imageView.setVisibility(View.GONE);
            exoPlayerView.setVisibility(View.VISIBLE);
            playVideo(Uri.parse(videoLink),exoPlayerView);
        }
        problemTv.setText(problem);
        addressTv.setText(address);
        ratingTv.setText(getString(R.string.ratingText )+ rating+getString(R.string.outOf5Text));
        mainUserName.setText(mainUserFullName);
        Picasso.with(this).load(mainUserImageLink).error(R.drawable.error).into(mainUserImage);
        if(progressDialog.isShowing())
            progressDialog.dismiss();

    }

    private void playVideo(Uri uri, SimpleExoPlayerView mExoPlayerView) {
        if(mExoPlayer !=null){
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        android.os.Handler handler = new android.os.Handler();
        TrackSelector trackSelector =  new DefaultTrackSelector(handler,videoTrackSelectionFactory);
        LoadControl loadControl= new DefaultLoadControl();
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(this,trackSelector,loadControl);
        mExoPlayerView.setPlayer(mExoPlayer);
        String userAgent = Util.getUserAgent(this,"BEcoFriend");
        if(uri!=null){
            MediaSource mediaSource = new ExtractorMediaSource(uri,new DefaultDataSourceFactory(this,userAgent),new DefaultExtractorsFactory(),null,null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        }
        else{
            Toast.makeText(this, getString(R.string.videoLinkProblemText), Toast.LENGTH_SHORT).show();
        }
    }
}
