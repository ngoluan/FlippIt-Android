package luan.com.flippit.widget;/*
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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;

import luan.com.flippit.GeneralUtilities;
import luan.com.flippit.HistoryItem;
import luan.com.flippit.MyActivity;
import luan.com.flippit.R;
import luan.com.flippit.utilities.CopyClipboard;
import luan.com.flippit.utilities.DeleteHistory_v2;
import luan.com.flippit.utilities.HistoryInterface;
import luan.com.flippit.utilities.OpenFile;
import luan.com.flippit.utilities.SendItem;
import luan.com.flippit.utilities.ShareItem;
import luan.com.flippit.utilities.UpdateHistoryListview_v2;

/**
 * The weather widget's AppWidgetProvider.
 */
public class WidgetProvider extends AppWidgetProvider {
    static Context mContext = null;
    static RemoteViews views = null;
    static ArrayList<HistoryItem> historyItems = new ArrayList<HistoryItem>();
    static AppWidgetManager mAppWidgetManager = null;
    static int appWidgetId = 0;
    static int[] mAppWidgetIds = null;
    static GetDataCallback getDataCallback = null;
    static UpdateHistoryListview_v2 updateHistoryListview = null;
    static String email = "";

    public WidgetProvider() {

    }

    public static void updateWidget(int[] localMAppWidgetIds) {
        final int N = localMAppWidgetIds.length;

        if (views == null) {
            views = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout);
        }

        for (int i = 0; i < N; i++) {
            appWidgetId = localMAppWidgetIds[i];

            Intent intent = new Intent(mContext, CustomWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, localMAppWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(R.id.listViewWidget, intent);

            Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Updating listview for :" + appWidgetId);

            Intent rowIntent = new Intent(mContext, WidgetProvider.class);
            rowIntent.setAction("row_action");
            rowIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, localMAppWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(mContext, 0, rowIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.listViewWidget, toastPendingIntent);

            Intent refreshIntent = new Intent(mContext, WidgetProvider.class);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            refreshIntent.setAction("Refresh");
            PendingIntent pendingRefreshIntent = PendingIntent.getBroadcast(mContext, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.refreshButton, pendingRefreshIntent);

            Intent openIntent = new Intent(mContext, MyActivity.class);
            PendingIntent pendingOpenIntent = PendingIntent.getActivity(mContext, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.logo, pendingOpenIntent);
            views.setOnClickPendingIntent(R.id.title, pendingOpenIntent);

            views.setViewVisibility(R.id.progressBar, View.INVISIBLE);
            views.setProgressBar(R.id.progressBar, 0, 0, false);

            mAppWidgetManager.notifyAppWidgetViewDataChanged(localMAppWidgetIds, R.id.listViewWidget);
            mAppWidgetManager.updateAppWidget(appWidgetId, WidgetProvider.views);
        }
    }

    public static void getData(int [] localAppWidgetIds) {
        Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Getting data. WidgetID " + localAppWidgetIds[0]);
        getDataCallback = new GetDataCallback(mContext, localAppWidgetIds);
        updateHistoryListview = new UpdateHistoryListview_v2(getDataCallback);
        updateHistoryListview.updateListview(10, email, mContext);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(MyActivity.TAG, getClass().getName() + ": " + "Received intent action: " + intent.getAction());
        mContext = context;

        SharedPreferences mPrefs = mContext.getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        email = mPrefs.getString("email", "");

        String historyResults = mPrefs.getString("historyResult", "");
        if (!historyResults.equals("")) {
            WidgetProvider.historyItems = GeneralUtilities.historyJSONtoArray(historyResults);
        }


        mAppWidgetManager = AppWidgetManager.getInstance(context);
        Bundle extras = intent.getExtras();
        int widgetId = 0;
        int[] localWidgetIds = null;
        if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            widgetId = intent.getIntExtra(mAppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            Log.i(MyActivity.TAG, getClass().getName() + ": " + "WidgetID: " + String.valueOf(widgetId));
            localWidgetIds = new int[]{widgetId};
        }

        if (intent.getAction().equals("row_action")) {
            int viewIndex = intent.getIntExtra("position", 0);
            String viewAction = intent.getStringExtra("action");

            Log.i(MyActivity.TAG, getClass().getName() + ": " + "Row action: " + viewAction);
            if (viewAction.equals("copy")) {
                new CopyClipboard(WidgetProvider.historyItems.get(viewIndex).message, mContext);
            } else if (viewAction.equals("send")) {
                new SendItem(WidgetProvider.historyItems.get(viewIndex), mContext);
            } else if (viewAction.equals("share")) {
                new ShareItem(WidgetProvider.historyItems.get(viewIndex), mContext);
            } else if (viewAction.equals("delete")) {
                DeleteCallback deleteHistoryCallback = new DeleteCallback(mContext, localWidgetIds);
                new DeleteHistory_v2(WidgetProvider.historyItems.get(viewIndex).dbID, viewIndex, mContext, deleteHistoryCallback);
            } else if (viewAction.equals("open")) {
                new OpenFile(WidgetProvider.historyItems.get(viewIndex), mContext);
            }
        } else if (intent.getAction().equals("Refresh")) {
            getData(localWidgetIds);
        }
        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        mAppWidgetIds = appWidgetIds;
        mContext = context;
        mAppWidgetManager = appWidgetManager;

        SharedPreferences mPrefs = mContext.getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        String email = mPrefs.getString("email", "");

        if (views == null) {
            views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        }

        views.setViewVisibility(R.id.progressBar, View.VISIBLE);
        views.setProgressBar(R.id.progressBar, 0, 0, true);

        getData(mAppWidgetIds);
    }

    public static class GetDataCallback extends HistoryInterface {
        int[] localmAppWidgetIds = null;

        public GetDataCallback(Context context, int[] mAppWidgetIds) {
            super(context);
            this.localmAppWidgetIds = mAppWidgetIds;
            Log.i(MyActivity.TAG, getClass().getName() + ": " + "Callback: " + String.valueOf(localmAppWidgetIds[0]));
        }

        @Override
        public void callBackProgress(int progress) {

        }

        @Override
        public void callBackFinish(Bundle extras) {

        }

        @Override
        public void callBackFinish(ArrayList<HistoryItem> historyItems) {
            Log.i(MyActivity.TAG, getClass().getName() + ": " + "Received items: " + String.valueOf(historyItems.size() + " " + localmAppWidgetIds[0]));

            WidgetProvider.historyItems = historyItems;
            WidgetProvider.updateWidget(this.localmAppWidgetIds);
        }
    }

    public static class DeleteCallback extends HistoryInterface {
        int[] mAppWidgetIds = null;

        public DeleteCallback(Context context, int[] mAppWidgetIds) {
            super(context);
            this.mAppWidgetIds = mAppWidgetIds;
        }

        @Override
        public void callBackProgress(int progress) {

        }

        @Override
        public void callBackFinish(Bundle extras) {
            int position = extras.getInt("position");
            WidgetProvider.historyItems.remove(position);
            WidgetProvider.updateWidget(mAppWidgetIds);
        }

        @Override
        public void callBackFinish(ArrayList<HistoryItem> historyItems) {

        }
    }
}