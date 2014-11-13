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
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;

import luan.com.pass.HistoryFragment;
import luan.com.pass.HistoryItem;
import luan.com.pass.MyActivity;
import luan.com.pass.R;
import luan.com.pass.utilities.CopyClipboard;
import luan.com.pass.utilities.DeleteHistory;
import luan.com.pass.utilities.HistoryGetCallbackInterface;
import luan.com.pass.utilities.OpenFile;
import luan.com.pass.utilities.SendItem;
import luan.com.pass.utilities.ShareItem;
import luan.com.pass.utilities.UpdateHistoryListview;

/**
 * The weather widget's AppWidgetProvider.
 */
public class WidgetProvider extends AppWidgetProvider {
    public static final String TOAST_ACTION = "luan.com.pass.TOAST_ACTION";
    static Context mContext = null;
    static RemoteViews views=null;
    static ArrayList<HistoryItem> historyItems= new ArrayList<HistoryItem>();
    static AppWidgetManager mAppWidgetManager=null;
    static int appWidgetId = 0;
    static int[] mAppWidgetIds = null;
    public WidgetProvider() {

    }

    public static void updateWidget() {

        final int N = mAppWidgetIds.length;
        for (int i = 0; i < N; i++) {
            appWidgetId = mAppWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(mContext, CustomWidgetService.class);
            views.setRemoteAdapter(R.id.listViewWidget, intent);

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            Intent rowIntent = new Intent(mContext, WidgetProvider.class);
            rowIntent.setAction("row_action");
            rowIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(mContext, 0, rowIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.listViewWidget, toastPendingIntent);

            Intent refreshIntent = new Intent(mContext, WidgetProvider.class);
            refreshIntent.setAction("Refresh");
            PendingIntent pendingRefreshIntent = PendingIntent.getBroadcast(mContext, 0, refreshIntent, 0);
            views.setOnClickPendingIntent(R.id.refreshButton, pendingRefreshIntent);

            Intent openIntent = new Intent(mContext, MyActivity.class);
            PendingIntent pendingOpenIntent = PendingIntent.getActivity(mContext, 0, openIntent, 0);
            views.setOnClickPendingIntent(R.id.logo, pendingOpenIntent);
            views.setOnClickPendingIntent(R.id.title, pendingOpenIntent);

            views.setViewVisibility(R.id.progressBar, View.INVISIBLE);
            views.setProgressBar(R.id.progressBar,0,0,false);

            WidgetProvider.mAppWidgetManager.updateAppWidget(appWidgetId, WidgetProvider.views);

        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        Log.i(MyActivity.TAG, getClass().getName() + ": " + "Received intent action:" + intent.getAction());

        if (intent.getAction().equals("row_action")) {

            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int viewIndex = intent.getIntExtra("position", 0);
            String viewAction = intent.getStringExtra("action");

            Log.i(MyActivity.TAG, getClass().getName() + ": " + "Size:" +viewAction);
            if(viewAction.equals("copy")){
                new CopyClipboard(WidgetProvider.historyItems.get(viewIndex).message, mContext);
            }
            else if(viewAction.equals("send")){
                new SendItem(WidgetProvider.historyItems.get(viewIndex), mContext);
            }
            else if(viewAction.equals("share")){
                new ShareItem(WidgetProvider.historyItems.get(viewIndex), mContext);
            }
            else if(viewAction.equals("delete")){
                HistoryGetCallbackInterface deleteHistoryCallback = new DeleteHistoryCallback();
                new DeleteHistory(WidgetProvider.historyItems.get(viewIndex).dbID, viewIndex, mContext, deleteHistoryCallback);
            }
            else if(viewAction.equals("open")){
                new OpenFile(WidgetProvider.historyItems.get(viewIndex), mContext);
            }
        } else if (intent.getAction().equals("Refresh")) {
            onUpdate(mContext, mAppWidgetManager,mAppWidgetIds);
        }
        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        mAppWidgetIds = appWidgetIds;
        mContext=context;
        mAppWidgetManager=appWidgetManager;
        SharedPreferences mPrefs = context.getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        views.setViewVisibility(R.id.progressBar, View.VISIBLE);
        views.setProgressBar(R.id.progressBar,0,0,true);

        String email = mPrefs.getString("email", "");
        WidgetGetCallback widgetGetCallback = new WidgetGetCallback();
        new UpdateHistoryListview(20, email, null, null,null,widgetGetCallback );
        // Perform this loop procedure for each App Widget that belongs to this provider

    }

    public static class WidgetGetCallback implements HistoryGetCallbackInterface {
        @Override
        public void callBack(ArrayList<HistoryItem> historyItems) {
            WidgetProvider.historyItems=historyItems;
            updateWidget();

        }

        @Override
        public void callBack(int i) {

        }
    }
    public static class DeleteHistoryCallback implements  HistoryGetCallbackInterface{
        @Override
        public void callBack(ArrayList<HistoryItem> historyItems) {

        }

        @Override
        public void callBack(int i) {
            WidgetProvider.historyItems.remove(i);
            WidgetProvider.updateWidget();
        }
    }
}