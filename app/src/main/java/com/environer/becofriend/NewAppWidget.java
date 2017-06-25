package com.environer.becofriend;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.environer.becofriend.model.PostContents;
import com.environer.becofriend.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;
import static com.environer.becofriend.utils.Constants.ADDRESS;
import static com.environer.becofriend.utils.Constants.CANCLED;
import static com.environer.becofriend.utils.Constants.CITY;
import static com.environer.becofriend.utils.Constants.MAIN_USER;
import static com.environer.becofriend.utils.Constants.OK;
import static com.environer.becofriend.utils.Constants.PROBLEM;
import static com.environer.becofriend.utils.Constants.RATING;
import static com.environer.becofriend.utils.Constants.SHARED_PREFERENCES;
import static com.environer.becofriend.utils.Constants.STATUS;
import static com.environer.becofriend.utils.Constants.USER_NAME;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    static boolean isProfileOK;
    static String[] userInfo;
    static private long childrens;
    static private int count;
    static PostContents contents;
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.quote);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        if(!isProfileOK)
            views.setTextViewText(R.id.problemText, widgetText);
        else{
            //do your work
//            Toast.makeText(context, "Profile set", Toast.LENGTH_SHORT).show();
//            views.setTextViewText(R.id.appwidget_text, "Hello World");
            getDataRandom(context,views,appWidgetManager,appWidgetId);


        }

        // Instruct the widget manager to update the widget

    }
    private static void getDataRandom(final Context context, final RemoteViews views, final AppWidgetManager appWidgetManager, final int appWidgetId){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(CITY).child(userInfo[0]).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    childrens = dataSnapshot.getChildrenCount();
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        contents = new PostContents();
                        count = 0;
                        for(DataSnapshot d: ds.getChildren()){
                            if(d.getKey().equals(ADDRESS)) {
                                contents.setAddress(d.getValue().toString());
                                views.setTextViewText(R.id.addressText,context.getString(R.string.address)+context.getString(R.string.colon) + contents.getAddress());
                                count++;
                            }
                            else if(d.getKey().equals(MAIN_USER))
                            {
                                contents.setMainUser(d.getValue().toString());
                                views.setTextViewText(R.id.reportedBy,context.getString(R.string.reportedByLabel) + contents.getMainUser());
                                count++;
                            }
                            else if(d.getKey().equals(RATING))
                            {
                                contents.setRating(d.getValue().toString());
                                views.setTextViewText(R.id.ratingText, contents.getRating());
                                count++;

                            }
                            else if(d.getKey().equals(PROBLEM))
                            {
                                contents.setProblem(d.getValue().toString());
                                views.setTextViewText(R.id.problemText,context.getString(R.string.problem)+context.getString(R.string.colon) + contents.getProblem());
                                count++;
                            }
                            views.setTextViewText(R.id.totalChildrenText, context.getString(R.string.totalChild) + String.valueOf(childrens));
                            if(count == 4){
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        }
                        break;

                    }
                }
                else{
                    views.setTextViewText(R.id.problemText,context.getString(R.string.noPostsText));
                    appWidgetManager.updateAppWidget(appWidgetId,views);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean checkProfile(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PROFILE_STATUS_PREFERENCE, MODE_PRIVATE);
        String result = sharedPreferences.getString(STATUS,CANCLED);
        if(result.equals(OK))
            return true;
        return false;
    }

    public String[] getUserInfo(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES,MODE_PRIVATE);
        String[] info = new String[2];
        info[0] = sharedPref.getString(CITY,"Other");
        info[1] = sharedPref.getString(USER_NAME,"SomeOne");
        return info;
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            if(checkProfile(context)){
                isProfileOK = true;
                userInfo = getUserInfo(context);
            }
            else{
                Toast.makeText(context, context.getString(R.string.widgetNotice), Toast.LENGTH_SHORT).show();
            }
            updateAppWidget(context, appWidgetManager, appWidgetId);

        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

