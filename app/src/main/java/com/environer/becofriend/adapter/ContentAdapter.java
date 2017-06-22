package com.environer.becofriend.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.environer.becofriend.DetailActivity;
import com.environer.becofriend.R;
import com.environer.becofriend.model.PostContents;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;
import static android.support.v7.widget.RecyclerView.*;
import static com.environer.becofriend.utils.Constants.*;

/**
 * Created by Mohammad Adil on 20-06-2017.
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.MyViewHolder> {

    Context context;
    ArrayList<PostContents> myDatabase;
    public static SimpleExoPlayer mExoPlayer;
    DatabaseReference mDatabase;
    DatabaseReference currentDB;
    boolean isTabletLand;

    ImageView imageViewDtl,imageViewMapDtl;
    CircleImageView mainUserImageDtl;
    SimpleExoPlayerView exoPlayerViewDtl;
    TextView problemDtl,addressDtl,ratingDtl,fullNameDtl;
    private FloatingActionButton email_fab;

    public ContentAdapter(Context con, ArrayList<PostContents> data){
        myDatabase = data;
        context = con;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        isTabletLand = context.getResources().getBoolean(R.bool.isTabletLandscape);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType){
            case 0:
            {
                View view = LayoutInflater.from(context).inflate(R.layout.image_item,parent,false);
                ImageViewHolder imageViewHolder = new ImageViewHolder(view);
                return imageViewHolder;
            }
            case 1:
            {
                View view = LayoutInflater.from(context).inflate(R.layout.video_item,parent,false);
                VideoViewHolder videoViewHolder = new VideoViewHolder(view);
                return videoViewHolder;
            }
        }
        return null;

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PostContents currentContent = myDatabase.get(position);
        int type = holder.getItemViewType();
        switch (type){
            case 0:
            {
                populateImageView(holder,currentContent);
                break;
            }
            case 1:{
                populateVideoView(holder,currentContent);
                break;
            }


        }
    }

    private void populateVideoView(final MyViewHolder holder, final PostContents currentCon) {


        final VideoViewHolder videoViewHolder = (VideoViewHolder)holder;
        videoViewHolder.problemTv.setText(currentCon.getProblem());
        videoViewHolder.addressTv.setText(currentCon.getAddress());
        final Uri uri = Uri.parse(currentCon.getDownnloadLink());
        playVideo(uri,videoViewHolder.exoPlayerView);

        //exoplayer click listner is not working here
        videoViewHolder.imageCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PostContents clicked = myDatabase.get(videoViewHolder.getAdapterPosition());
                currentDB = mDatabase.child(CITY).child(clicked.getCity()).child(clicked.getKey());
                if(mExoPlayer!=null){
                    mExoPlayer.stop();
                    mExoPlayer.release();
                    mExoPlayer = null;
                }

                if(!isTabletLand)
                    LaunchDetailIntent(videoViewHolder.getAdapterPosition());
                else{
                    bindViews();//It will bind the views which was previously on another activity
                    getInfoAndPopluateViews(currentCon);
                }
            }
        });



        //Hide the rating bar if the content is rated by current user
        DetectRatingStatus(videoViewHolder.ratingBar);

        //Handle Rating Bar
        videoViewHolder.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(final RatingBar ratingBar, float v, boolean b) {


                PostContents clickedContent = myDatabase.get(videoViewHolder.getAdapterPosition());
                currentDB = mDatabase.child(CITY).child(clickedContent.getCity()).child(clickedContent.getKey());

                currentDB.child(RATING).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.getKey().equals(TOTAL_RATING))
                        {
                            double new_rating = (  (Double.valueOf(dataSnapshot.getValue().toString())) + ratingBar.getRating() ) /2;
                            currentDB.child(RATING).child(TOTAL_RATING).setValue(String.valueOf(new_rating));


                            //Add the rating user in the list so that I can hide the ratingbar next time
                            currentDB.child(RATING).child(getUserName()).setValue("1");
                            ratingBar.setVisibility(GONE);
                            Toast.makeText(context, "Thanks for rating: " + new_rating +"/5" , Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    public static SimpleExoPlayer returnInstance(){
        return mExoPlayer;
    }
    private void bindViews(){
        problemDtl = (TextView)((Activity)context).findViewById(R.id.textViewProblemDetail);
        imageViewDtl = (ImageView)((Activity)context).findViewById(R.id.imageViewDetail);
        exoPlayerViewDtl = (SimpleExoPlayerView)((Activity)context).findViewById(R.id.exoPlayerViewDetail);
        addressDtl = (TextView) ((Activity)context).findViewById(R.id.textViewAddressDetail);
        ratingDtl = (TextView)((Activity)context).findViewById(R.id.textViewRatingLabelDetail);
        mainUserImageDtl = (CircleImageView)((Activity)context).findViewById(R.id.circularImgMainUser);
        fullNameDtl = (TextView)((Activity)context).findViewById(R.id.textViewMainUDetail);
        imageViewMapDtl = (ImageView)((Activity)context).findViewById(R.id.imageViewMapDetail);
        email_fab = (FloatingActionButton)((Activity)context).findViewById(R.id.fabEmail);

    }

    private void getInfoAndPopluateViews(final PostContents currentCon) {
        problemDtl.setText(currentCon.getProblem());
        addressDtl.setText(currentCon.getAddress());
        mDatabase.child(USERS).child(currentCon.getMainUser()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fullNameDtl.setText(dataSnapshot.child(FULL_NAME).getValue().toString());
                String imgUrl = dataSnapshot.child(MAINUSER_IMAGELINK).getValue().toString();
                Picasso.with(context).load(imgUrl).error(R.drawable.error).into(mainUserImageDtl);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currentDB.child(RATING).child(TOTAL_RATING).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ratingDtl.setText("Rating: " + dataSnapshot.getValue().toString() + " out of 5.0");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        imageViewMapDtl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMapIntent(currentCon.getLatitude(),currentCon.getLongitude());
            }
        });
        final String content = currentCon.getDownnloadLink();
        if(content.contains("IMG")) {
            exoPlayerViewDtl.setVisibility(GONE);
            Picasso.with(context).load(content).error(R.drawable.error).into(imageViewDtl);
        }
        else{
            imageViewDtl.setVisibility(GONE);
            exoPlayerViewDtl.setVisibility(VISIBLE);
            playVideo(Uri.parse(content),exoPlayerViewDtl);

        }
        email_fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                launchEmailIntent(content,currentCon.getProblem(),currentCon.getAddress());
            }
        });

    }
    private void launchEmailIntent(String contentLink, String problem,String address) {

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","type_here@gmail.com",null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Serious issue noticed by Environer.Inc");
        emailIntent.putExtra(Intent.EXTRA_TEXT,MESSAGE+"\n" + PROBLEM  + ": " + problem + "\n"
                + "Content link: " + contentLink + "\n" +
                ADDRESS + " : " +
                address + "\n");
        context.startActivity(Intent.createChooser(emailIntent,"Send email..."));
    }

    private void launchMapIntent(String latitude,String longitude) {
        if(mExoPlayer!=null){
            mExoPlayer.release();
            mExoPlayer.stop();
            mExoPlayer = null;
        }
        double lat = Double.valueOf(latitude);
        double longi = Double.valueOf(longitude);
        String label = "BEcoFriend";
        String uriBegin = "geo:" + lat + "," + longi;
        String query = lat + "," + longi + "(" + label + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        context.startActivity(intent);

    }

    private void playVideo(Uri uri,SimpleExoPlayerView mExoPlayerView) {
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
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(context,trackSelector,loadControl);
        mExoPlayerView.setPlayer(mExoPlayer);
        String userAgent = Util.getUserAgent(context,"BEcoFriend");
        if(uri!=null){
            MediaSource mediaSource = new ExtractorMediaSource(uri,new DefaultDataSourceFactory(context,userAgent),new DefaultExtractorsFactory(),null,null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        }
        else{
            Toast.makeText(context, "Problem in video link", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateImageView(MyViewHolder holder, final PostContents currentContent) {
        final ImageViewHolder imageViewHolder = (ImageViewHolder)holder;
        imageViewHolder.problemTv.setText(currentContent.getProblem());
        imageViewHolder.addressTv.setText(currentContent.getAddress());
        Picasso p = new Picasso.Builder(context).memoryCache(new LruCache(24000)).build();
        p.load(currentContent.getDownnloadLink()).error(R.drawable.error).into(imageViewHolder.imageView);

        imageViewHolder.imageCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PostContents clicked = myDatabase.get(imageViewHolder.getAdapterPosition());
                currentDB = mDatabase.child(CITY).child(clicked.getCity()).child(clicked.getKey());
                if(!isTabletLand)
                    LaunchDetailIntent(imageViewHolder.getAdapterPosition());
                else{
                    bindViews();//It will bind the views which was previously on another activity
                    getInfoAndPopluateViews(currentContent);
                }

            }
        });
        //If user has already rated the content then hide the rating view;
        currentDB = mDatabase.child(CITY).child(currentContent.getCity()).child(currentContent.getKey());
        DetectRatingStatus(imageViewHolder.ratingBar);
        imageViewHolder.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(final RatingBar ratingBar, float v, boolean b) {


                //Update according to the clicked view
                PostContents clickedContent = myDatabase.get(imageViewHolder.getAdapterPosition());
                currentDB = mDatabase.child(CITY).child(clickedContent.getCity()).child(clickedContent.getKey());
                currentDB.child(RATING).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.getKey().equals(TOTAL_RATING))
                        {
                            double new_rating = (  (Double.valueOf(dataSnapshot.getValue().toString())) + ratingBar.getRating() ) /2;
                            currentDB.child(RATING).child(TOTAL_RATING).setValue(String.valueOf(new_rating));


                            //Add the rating user in the list so that I can hide the ratingbar next time
                            currentDB.child(RATING).child(getUserName()).setValue("1");
                            ratingBar.setVisibility(GONE);
                            Toast.makeText(context, "Thanks for rating: " + new_rating +"/5" , Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void DetectRatingStatus(final RatingBar ratingBar) {
        currentDB.child(RATING).child(getUserName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    ratingBar.setVisibility(GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getUserName(){
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        String user = sharedPref.getString(USER_NAME,"SomeOne");
        return user;
    }
    private void LaunchDetailIntent(int clickedPos) {
        PostContents current = myDatabase.get(clickedPos);
        final Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(PROBLEM,current.getProblem());
        intent.putExtra(ADDRESS,current.getAddress());
        intent.putExtra(LATITUDE,current.getLatitude());
        intent.putExtra(LONGITUDE,current.getLongitude());
        intent.putExtra(MAIN_USER,current.getMainUser());
        currentDB.child(RATING).child(TOTAL_RATING).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                intent.putExtra(RATING,dataSnapshot.getValue().toString());
                context.startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        String content = current.getDownnloadLink();
        if(content.contains("IMG"))
            intent.putExtra(POST_IMAGE,content);
        else
            intent.putExtra(POST_VIDEO,content);




    }

    @Override
    public int getItemViewType(int position) {
        String url = myDatabase.get(position).getDownnloadLink();
            if (url.contains("IMG"))
                return 0;
            return 1;

    }

    @Override
    public int getItemCount() {
        return myDatabase.size();
    }

    public class MyViewHolder extends ViewHolder{

        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ImageViewHolder extends MyViewHolder{

        ImageView imageView;
        TextView addressTv,problemTv;
        RatingBar ratingBar;
        CardView imageCardView;
        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.imageViewContent);
            addressTv = (TextView)itemView.findViewById(R.id.textViewAddressImage);
            problemTv = (TextView)itemView.findViewById(R.id.textViewProblemsImage);
            ratingBar = (RatingBar)itemView.findViewById(R.id.ratingBarImage);
            imageCardView = (CardView)itemView.findViewById(R.id.cardViewImage);
        }
    }

    public class VideoViewHolder extends MyViewHolder{

        SimpleExoPlayerView exoPlayerView;
        TextView addressTv,problemTv;
        RatingBar ratingBar;
        CardView imageCardView;
        public VideoViewHolder(View itemView) {
            super(itemView);
            exoPlayerView = (SimpleExoPlayerView)itemView.findViewById(R.id.exoPlayerV);
            addressTv = (TextView)itemView.findViewById(R.id.textViewAddressVideo);
            problemTv = (TextView)itemView.findViewById(R.id.textViewProblemVideo);
            ratingBar = (RatingBar)itemView.findViewById(R.id.ratingBarVideo);
            imageCardView = (CardView)itemView.findViewById(R.id.cardViewVideo);
        }
    }
}
