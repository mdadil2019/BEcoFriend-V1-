package com.environer.becofriend.adapter;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.environer.becofriend.R;
import com.environer.becofriend.model.PostContents;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
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
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static android.support.v7.widget.RecyclerView.*;

/**
 * Created by Mohammad Adil on 20-06-2017.
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.MyViewHolder> {

    Context context;
    ArrayList<PostContents> myDatabase;
    SimpleExoPlayer mExoPlayer;
    public ContentAdapter(Context con, ArrayList<PostContents> data){
        myDatabase = data;
        context = con;
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

    private void populateVideoView(MyViewHolder holder, PostContents currentCon) {
        VideoViewHolder videoViewHolder = (VideoViewHolder)holder;
        videoViewHolder.problemTv.setText(currentCon.getProblem());
        videoViewHolder.addressTv.setText(currentCon.getAddress());
        Uri uri = Uri.parse(currentCon.getDownnloadLink());
        playVideo(uri,videoViewHolder.exoPlayerView);
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

    private void populateImageView(MyViewHolder holder, PostContents currentContent) {
        ImageViewHolder imageViewHolder = (ImageViewHolder)holder;


        imageViewHolder.problemTv.setText(currentContent.getProblem());
        imageViewHolder.addressTv.setText(currentContent.getAddress());
        Picasso.with(context).load(currentContent.getDownnloadLink()).error(R.drawable.error).into(imageViewHolder.imageView);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0)
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
        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.imageViewContent);
            addressTv = (TextView)itemView.findViewById(R.id.textViewAddressImage);
            problemTv = (TextView)itemView.findViewById(R.id.textViewProblemsImage);
            ratingBar = (RatingBar)itemView.findViewById(R.id.ratingBarImage);
        }
    }

    public class VideoViewHolder extends MyViewHolder{

        SimpleExoPlayerView exoPlayerView;
        TextView addressTv,problemTv;
        RatingBar ratingBar;
        public VideoViewHolder(View itemView) {
            super(itemView);
            exoPlayerView = (SimpleExoPlayerView)itemView.findViewById(R.id.exoPlayerV);
            addressTv = (TextView)itemView.findViewById(R.id.textViewAddressVideo);
            problemTv = (TextView)itemView.findViewById(R.id.textViewProblemVideo);
            ratingBar = (RatingBar)itemView.findViewById(R.id.ratingBarVideo);
        }
    }
}
