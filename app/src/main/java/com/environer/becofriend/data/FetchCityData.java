package com.environer.becofriend.data;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.widget.Toast;

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

import static android.content.Context.MODE_PRIVATE;
import static com.environer.becofriend.utils.Constants.ADDRESS;
import static com.environer.becofriend.utils.Constants.CITY;
import static com.environer.becofriend.utils.Constants.LATITUDE;
import static com.environer.becofriend.utils.Constants.LONGITUDE;
import static com.environer.becofriend.utils.Constants.MAIN_USER;
import static com.environer.becofriend.utils.Constants.POST_IMAGE;
import static com.environer.becofriend.utils.Constants.POST_VIDEO;
import static com.environer.becofriend.utils.Constants.PROBLEM;
import static com.environer.becofriend.utils.Constants.RATING;
import static com.environer.becofriend.utils.Constants.SHARED_PREFERENCES;
import static com.environer.becofriend.utils.Constants.TOTAL_RATING;

/**
 * Created by Mohammad Adil on 20-06-2017.
 */

public class FetchCityData{
    Context context;
    ArrayList<PostContents> myData;
    PostContents dataModel;
    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    private int count;
    private long totalCount;
    boolean isLandscape;

    public FetchCityData(Context c, RecyclerView rView){
        context = c;
        recyclerView = rView;
    }
    DatabaseReference mDatabase;
    DatabaseReference cityRef;
    String userCity;


    private void beReady(){
        isLandscape = context.getResources().getBoolean(R.bool.isLandscape);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        getUserCity();
        cityRef = mDatabase.child(CITY).child(userCity);
        if(cityRef==null){
            Toast.makeText(context, "No Data in firebase database", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Retrieving your locality information...");
        progressDialog.show();
    }
    public void getData() {
        beReady();
        myData = new ArrayList<>();
        cityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        cityRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    storeDataInArray(dataSnapshot);

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

    private void storeDataInArray(final DataSnapshot dataSnapshot) {
        dataModel = new PostContents();
        dataModel.setKey(dataSnapshot.getKey());
        dataModel.setCity(userCity);
        //Since onChildAdded is called on (a)when we add the child of that parent(a) and the problem is ==> by adding only first child to
        // (a)parent(newly created), the onChildadded is called and we have to store all the information of that parent(a), so first check that if there
        // is 7 child or not else use a new database reference and setOnChildAdded on the newly added content
        if(dataSnapshot.getChildrenCount()==7) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
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
                else if(ds.getKey().equals(RATING)){
                    dataModel.setRating(ds.child(TOTAL_RATING).getValue().toString());

                }
                totalCount++;
                if(totalCount == 7){
                    myData.add(dataModel);

                    ContentAdapter adapter = new ContentAdapter(context,myData);
                    if(!isLandscape) {
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                        recyclerView.setLayoutManager(linearLayoutManager);
                    }
                    else{
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(context,numberofColumn());
                        recyclerView.setLayoutManager(gridLayoutManager);
                    }
                    recyclerView.setAdapter(adapter);
                    totalCount = 0;
                }

            }
        }
        else{
            DatabaseReference newChild = cityRef.child(dataSnapshot.getKey());
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
                    else if(ds.getKey().equals(RATING)){
                        dataModel.setRating(ds.child(TOTAL_RATING).getValue().toString());
                    }
                    count++;
                    if(count == 7){
                        myData.add(dataModel);

                        ContentAdapter adapter = new ContentAdapter(context,myData);
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
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if(nColumns<2)return 2;
        return nColumns;
    }

    void getUserCity(){
        SharedPreferences sharedPreferences =context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        userCity = sharedPreferences.getString(CITY,"Other");
    }
}
