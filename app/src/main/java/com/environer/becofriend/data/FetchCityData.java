package com.environer.becofriend.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

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
import static com.environer.becofriend.utils.Constants.SHARED_PREFERENCES;

/**
 * Created by Mohammad Adil on 20-06-2017.
 */

public class FetchCityData extends AsyncTask<Void,Void,Void> {
    Context context;
    ArrayList<PostContents> myData;
    PostContents dataModel;
    RecyclerView recyclerView;
    ProgressDialog progressDialog;

    public FetchCityData(Context c, RecyclerView rView){
        context = c;
        recyclerView = rView;
    }
    DatabaseReference mDatabase;
    DatabaseReference cityRef;
    String userCity;
    @Override
    protected void onPreExecute() {
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

    @Override
    protected Void doInBackground(Void... voids) {
        getData();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {


    }

    private void getData() {
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

    private void storeDataInArray(DataSnapshot dataSnapshot) {
        dataModel = new PostContents();
        if(dataSnapshot.getChildrenCount()==6) {
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
        myData.add(dataModel);

        ContentAdapter adapter = new ContentAdapter(context,myData);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    void getUserCity(){
        SharedPreferences sharedPreferences =context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        userCity = sharedPreferences.getString(CITY,"Other");
    }
}
