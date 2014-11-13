package luan.com.pass.widget;/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

import java.util.ArrayList;

import luan.com.pass.HistoryItem;
import luan.com.pass.MyActivity;
import luan.com.pass.R;
import luan.com.pass.utilities.HistoryGetCallbackInterface;
import luan.com.pass.utilities.UpdateHistoryListview;

/**
 * The weather widget's AppWidgetProvider.
 */
public class WidgetProvider extends AppWidgetProvider {
    static Context mContext = null;
    static RemoteViews views=null;
    static ArrayList<HistoryItem> historyItems= new ArrayList<HistoryItem>();
    static AppWidgetManager mAppWidgetManager=null;
    static int appWidgetId = 0;
    public WidgetProvider() {

    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        mContext=context;
        mAppWidgetManager=appWidgetManager;
        SharedPreferences mPrefs = context.getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        String email = mPrefs.getString("email", "");
        WidgetGetCallback widgetGetCallback = new WidgetGetCallback();
        new UpdateHistoryListview(20, email, null, null,null,widgetGetCallback );
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, MyActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            //views.setOnClickPendingIntent(R.id.button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    public static class WidgetGetCallback implements HistoryGetCallbackInterface {
        @Override
        public void callBack(ArrayList<HistoryItem> historyItems) {
            WidgetProvider.historyItems=historyItems;
            WidgetProvider.mAppWidgetManager.updateAppWidget(appWidgetId, WidgetProvider.views);
        }
    }
}