package com.environer.becofriend.data;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.environer.becofriend.ContentActivity;
import com.environer.becofriend.R;
import com.environer.becofriend.adapter.ContentAdapter;
import com.environer.becofriend.model.PostContents;
import com.environer.becofriend.utils.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.environer.becofriend.utils.Constants.ADDRESS;
import static com.environer.becofriend.utils.Constants.CITY;
import static com.environer.becofriend.utils.Constants.LATITUDE;
import static com.environer.becofriend.utils.Constants.LONGITUDE;
import static com.environer.becofriend.utils.Constants.MAIN_USER;
import static com.environer.becofriend.utils.Constants.POST_IMAGE;
import static com.environer.becofriend.utils.Constants.POST_VIDEO;
import static com.environer.becofriend.utils.Constants.PROBLEM;
import static com.environer.becofriend.utils.Constants.RATING;
import static com.environer.becofriend.utils.Constants.TOTAL_RATING;

/**
 * Created by Mohammad Adil on 22-06-2017.
 */

public class FetchAllData {
    Context context;
    RecyclerView recyclerView;
    ArrayList<PostContents> allData;
    PostContents dataModel;
    DatabaseReference mDatabase;
    public static ProgressDialog progressDialog;
    boolean isLandscape ;
    private int totalCount;
    private int count;

    public FetchAllData(Context con, RecyclerView rcV){
        context = con;
        recyclerView = rcV;
    }

    private void getReady(){
       isLandscape = context.getResources().getBoolean(R.bool.isLandscape);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        allData = new ArrayList<>();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Retrieving world data... ");
        progressDialog.show();

    }

    public void getAllData() {
        getReady();


        mDatabase.child(CITY).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists())
                    getCityData(dataSnapshot);
                else
                    Toast.makeText(context, "No any posts are available", Toast.LENGTH_SHORT).show();
                if(progressDialog.isShowing())
                    progressDialog.dismiss();

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

    private void getCityData(DataSnapshot dataSnapshot) {
        //We are on a city
        //dataSnapshot==> Refer to a city
        String cityName = dataSnapshot.getKey();
        for(DataSnapshot cityChilds: dataSnapshot.getChildren()){
            storeAllDataInArray(cityChilds,cityName);
        }
    }

    private void storeAllDataInArray(DataSnapshot cityChilds,String cityNm) {
        dataModel = new PostContents();
        dataModel.setKey(cityChilds.getKey());
        dataModel.setCity(cityNm);
        if(cityChilds.getChildrenCount()==7) {
            for(DataSnapshot elements: cityChilds.getChildren()) {
                if (elements.getKey().equals(ADDRESS))
                    dataModel.setAddress(elements.getValue().toString());
                else if (elements.getKey().equals(LATITUDE))
                    dataModel.setLatitude(elements.getValue().toString());
                else if (elements.getKey().equals(LONGITUDE))
                    dataModel.setLongitude(elements.getValue().toString());
                else if (elements.getKey().equals(MAIN_USER))
                    dataModel.setMainUser(elements.getValue().toString());
                else if (elements.getKey().equals(POST_IMAGE) || elements.getKey().equals(POST_VIDEO))
                    dataModel.setDownnloadLink(elements.getValue().toString());
                else if (elements.getKey().equals(PROBLEM))
                    dataModel.setProblem(elements.getValue().toString());
                else if (elements.getKey().equals(RATING)) {
                    dataModel.setRating(elements.child(TOTAL_RATING).getValue().toString());

                }
                totalCount++;
                if (totalCount == 7) {
                    allData.add(dataModel);
                    ContentAdapter adapter = new ContentAdapter(context, allData);
                    if (!isLandscape) {
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                        recyclerView.setLayoutManager(linearLayoutManager);
                    } else {
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, numberofColumn());
                        recyclerView.setLayoutManager(gridLayoutManager);
                    }
                    recyclerView.setAdapter(adapter);
                    totalCount = 0;
                }
            }
        }//end if
            else {
                DatabaseReference newChild = mDatabase.child(CITY).child(cityNm).child(cityChilds.getKey());
                newChild.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot ds, String s) {
                        if (ds.getKey().equals(ADDRESS))
                            dataModel.setAddress(ds.getValue().toString());
                        else if (ds.getKey().equals(LATITUDE))
                            dataModel.setLatitude(ds.getValue().toString());
                        else if (ds.getKey().equals(LONGITUDE))
                            dataModel.setLongitude(ds.getValue().toString());
                        else if (ds.getKey().equals(MAIN_USER))
                            dataModel.setMainUser(ds.getValue().toString());
                        else if (ds.getKey().equals(POST_IMAGE) || ds.getKey().equals(POST_VIDEO))
                            dataModel.setDownnloadLink(ds.getValue().toString());
                        else if (ds.getKey().equals(PROBLEM))
                            dataModel.setProblem(ds.getValue().toString());
                        else if (ds.getKey().equals(RATING)) {
                            dataModel.setRating(ds.child(TOTAL_RATING).getValue().toString());
                        }
                        count++;
                        if(count == 7){
                            allData.add(dataModel);

                            ContentAdapter adapter = new ContentAdapter(context,allData);
                            if(!isLandscape) {
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                                recyclerView.setLayoutManager(linearLayoutManager);
                            }
                            else{
                                GridLayoutManager gridLayoutManager = new GridLayoutManager(context,numberofColumn());
                                recyclerView.setLayoutManager(gridLayoutManager);
                            }
                            recyclerView.setAdapter(adapter);
                            count=0;
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
        }


    private int numberofColumn(){
        return 2;
    }
//    DisplayMetrics displayMetrics = new DisplayMetrics();
//        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//    int widthDivider = 400;
//    int width = displayMetrics.widthPixels;
//    int nColumns = width / widthDivider;
//        if(nColumns<2)return 2;
//        return nColumns;
}
