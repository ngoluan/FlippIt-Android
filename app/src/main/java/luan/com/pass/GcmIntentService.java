/*
 * Copyright (C) 2013 The Android Open Source Project
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

package luan.com.pass;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import luan.com.pass.utilities.CopyClipboard;
import luan.com.pass.utilities.SendFileNotificationInterface;
import luan.com.pass.utilities.SendFileUpdateNotificationInterface;
import luan.com.pass.utilities.SendImageNotificationInterface;
import luan.com.pass.utilities.SendImageUpdateNotificationInterface;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    static public NotificationManager mNotificationManager;
    static Context mContext = null;
    static NotificationCompat.Builder mBuilder = null;
    static SharedPreferences mPrefs = null;
    static ActivityManager activityManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = this;
        mPrefs = getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String message = intent.getStringExtra("message");
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                manageNotification(message);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    private void manageNotification(String content) {
        JSONObject data = null;
        String msg = null;
        String fileName = null;
        try {
            data = new JSONObject(content);

            Log.i(MyActivity.TAG, getClass().getName() + ": " + "Received full message: " + data);

            msg = data.getString("message");
            msg = java.net.URLDecoder.decode(msg, "UTF-8");

            fileName = data.getString("fileName");

            String type = GeneralUtilities.typeOfMessage(fileName);

/*            if (fileName.equals("")) {
                String extraFileName = searchForExtra(msg);
                String extraType = extraMIMEOnline(extraFileName);

                Log.i(MyActivity.TAG, "Extra type " + type);
                if (!extraType.equals("")) {
                    fileName = msg;
                    type = extraType;
                }

            }*/
            if (type.contains("image")) {
                imgNotification(fileName, msg);
            } else if (type.equals("file")) {
                fileNotification(fileName, msg);
            } else if (type.equals("web")) {
                webNotification(msg);
            } else {
                String extraFileName = searchForExtra(msg);
                String extraType = extraMIMEOnline(extraFileName);

                if (!extraType.equals("")) {
                    extraDialog(msg, extraFileName, extraType);
                } else {
                    textNotification(msg);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    private void extraDialog(String msg, String extraFilename, String extraType) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MyActivity.class), 0);

        Log.i(MyActivity.TAG, "Extra notification : " + msg);

        msg = msg + " Could be: " + extraType;

        String url = msg;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        PendingIntent pendingWeb = PendingIntent.getActivity(mContext, 0, i, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.action_icon)
                        .setContentTitle("Pass")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .addAction(R.drawable.download_white, "Download", pendingWeb)
                        .addAction(R.drawable.send_white, "Open", pendingWeb)
                        .addAction(R.drawable.copy_white, "Copy", pendingWeb)
                        .setContentText("Rich message");

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.cancel(1);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        UpdateHistory updateHistory = new UpdateHistory();
        updateHistory.updateHistory(mContext);

    }

    private void textNotification(String msg) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MyActivity.class), 0);

        Log.i(MyActivity.TAG, "Text message: " + msg);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.action_icon)
                        .setContentTitle("Pass")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.cancel(1);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        new CopyClipboard(msg, mContext);
        UpdateHistory updateHistory = new UpdateHistory();
        updateHistory.updateHistory(mContext);
    }

    private void webNotification(String msg) {

    }

    private void imgNotification(String fileName, String msg) {
        mBuilder.setContentTitle("Pass")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.action_icon);
        Log.i(MyActivity.TAG, getClass().getName() + ": " + "Creating image notification");
        String email = mPrefs.getString("email", "");
        DownloadFiles downloadFiles = new DownloadFiles();
        MyActivity.Callback sendImageNotificationInterface = new SendImageNotificationInterface();
        MyActivity.Callback sendImageUpdateNotificationInterface = new SendImageUpdateNotificationInterface();
        downloadFiles.getImageFromServer(email, fileName, null, msg, sendImageNotificationInterface, sendImageUpdateNotificationInterface,
                mContext, mNotificationManager, mBuilder);
    }

    private void fileNotification(final String fileName, String msg) {
        mBuilder.setContentTitle("Pass")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.action_icon);
        Log.i(MyActivity.TAG, getClass().getName() + ": " + "Creating file notification");
        String email = mPrefs.getString("email", "");
        DownloadFiles downloadFiles = new DownloadFiles();
        MyActivity.Callback sendFileNotificationInterface = new SendFileNotificationInterface();
        MyActivity.Callback sendFileUpdateNotificationInterface = new SendFileUpdateNotificationInterface();
        downloadFiles.getFileFromServer(email, fileName, null, msg, sendFileNotificationInterface, sendFileUpdateNotificationInterface,
                mContext, mNotificationManager, mBuilder);
    }

    private String searchForExtra(String message) {
        int start = -1;
        int end = 0;
        String urlStr = "";
        if (message.indexOf("http") > -1) {
            start = message.indexOf("http");
        } else if (message.indexOf("https") > -1) {
            start = message.indexOf("https");
        } else if (message.indexOf("www") > -1) {
            start = message.indexOf("www");
        }
        Log.i(MyActivity.TAG, getClass().getName() + ": " + " start : " + start);
        if (start > -1) {
            end = message.indexOf(" ", start);
            if (end == -1) {
                end = message.length();
            }
            urlStr = message.substring(start, end);
            Log.i(MyActivity.TAG, getClass().getName() + ": " + " Extra content in message detected: " + urlStr);
        }
        return urlStr;
    }

    private String extraMIMEOnline(String urlStr) {
        String contentType = "";
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            contentType = connection.getContentType();
            Log.i(MyActivity.TAG, getClass().getName() + ": " + "Extra content type: " + contentType);
            if (!contentType.equals(null)) {
                contentType = "web";
            }
/*            if (!contentType.equals(null)) {
                if (contentType.contains("audio") == true || contentType.contains("video") == true || contentType.contains("application") == true) {
                    contentType = "file";
                } else if (contentType.contains("image") == true) {
                    contentType = "image";
                } else {
                    contentType = "web";
                }
            } else {
                contentType = "web";
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentType;
    }

}
