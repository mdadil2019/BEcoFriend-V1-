package com.environer.becofriend;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.environer.becofriend.utils.Constants;

import static android.content.Context.MODE_PRIVATE;
import static com.environer.becofriend.utils.Constants.CANCLED;
import static com.environer.becofriend.utils.Constants.CITY;
import static com.environer.becofriend.utils.Constants.OK;
import static com.environer.becofriend.utils.Constants.SHARED_PREFERENCES;
import static com.environer.becofriend.utils.Constants.STATUS;
import static com.environer.becofriend.utils.Constants.USER_NAME;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    static boolean isProfileOK;
    static String[] userInfo;
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.quote);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        if(!isProfileOK)
            views.setTextViewText(R.id.appwidget_text, widgetText);
        else{
            //do your work

        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
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

