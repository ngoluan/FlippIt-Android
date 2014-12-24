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
    GetDataCallback getDataCallback = null;
    UpdateHistoryListview_v2 updateHistoryListview = null;
    String email = "";

    public WidgetProvider() {

    }

    public static void updateWidget() {
        final int N = mAppWidgetIds.length;
        Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Updating listview with :" + mAppWidgetIds.length);
        for (int i = 0; i < N; i++) {
            appWidgetId = mAppWidgetIds[i];

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
            views.setProgressBar(R.id.progressBar, 0, 0, false);

            mAppWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetIds, R.id.listViewWidget);
            mAppWidgetManager.updateAppWidget(appWidgetId, WidgetProvider.views);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(MyActivity.TAG, getClass().getName() + ": " + "Received intent action: " + intent.getAction());
        mContext = context;

        SharedPreferences mPrefs = mContext.getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        email = mPrefs.getString("email", "");

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
                DeleteCallback deleteHistoryCallback = new DeleteCallback(mContext);
                new DeleteHistory_v2(WidgetProvider.historyItems.get(viewIndex).dbID, viewIndex, mContext, deleteHistoryCallback);
            } else if (viewAction.equals("open")) {
                new OpenFile(WidgetProvider.historyItems.get(viewIndex), mContext);
            }
        } else if (intent.getAction().equals("Refresh")) {
            getData();
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

        getData();
    }

    public void getData() {
        Log.i(MyActivity.TAG, getClass().getName() + ": " + "Getting data.");
        getDataCallback = new GetDataCallback(mContext);
        updateHistoryListview = new UpdateHistoryListview_v2(getDataCallback);
        updateHistoryListview.updateListview(10, email);
    }

    public static class GetDataCallback extends HistoryInterface {
        public GetDataCallback(Context context) {
            super(context);
        }

        @Override
        public void callBackProgress(int progress) {

        }

        @Override
        public void callBackFinish(Bundle extras) {

        }

        @Override
        public void callBackFinish(ArrayList<HistoryItem> historyItems) {
            Log.i(MyActivity.TAG, getClass().getName() + ": " + "Received items: " + String.valueOf(historyItems.size()));

            WidgetProvider.historyItems = historyItems;
            WidgetProvider.updateWidget();
        }
    }

    public static class DeleteCallback extends HistoryInterface {
        public DeleteCallback(Context context) {
            super(context);
        }

        @Override
        public void callBackProgress(int progress) {

        }

        @Override
        public void callBackFinish(Bundle extras) {
            int position = extras.getInt("position");
            WidgetProvider.historyItems.remove(position);
            WidgetProvider.updateWidget();
        }

        @Override
        public void callBackFinish(ArrayList<HistoryItem> historyItems) {

        }
    }
}