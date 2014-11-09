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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

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
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                manageNotification(message);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void manageNotification(String content) {
        JSONObject data = null;
        String msg = null;
        String fileName = null;
        try {
            data = new JSONObject(content);

            Log.i(MyActivity.TAG, getClass().getName()+": "+ "Received message: " + data);

            msg = data.getString("message");
            msg = java.net.URLDecoder.decode(msg, "UTF-8");
            fileName = data.getString("fileName");
            String type = MyActivity.typeOfMessage(fileName);

            if (type.equals("image")) {
                imgNotification(fileName, msg);
            } else if (type.equals("file")) {
                fileNotification(fileName, msg);
            } else {
                textNotification(msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


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
        copyClipboard(msg);
        UpdateHistory updateHistory=new UpdateHistory();
        updateHistory.updateHistory(mContext);
    }

    private void copyClipboard(String msg) {
        ClipboardManager clipboard = (ClipboardManager)
                mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Pass", msg);
        clipboard.setPrimaryClip(clip);
    }

    private void imgNotification(String fileName, String msg) {
        mBuilder.setContentTitle("Pass")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.action_icon);
        Log.i(MyActivity.TAG, getClass().getName()+": "+ "Creating image notification");
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
        Log.i(MyActivity.TAG, getClass().getName()+": "+ "Creating file notification");
        String email = mPrefs.getString("email", "");
        DownloadFiles downloadFiles = new DownloadFiles();
        MyActivity.Callback sendFileNotificationInterface = new SendFileNotificationInterface();
        MyActivity.Callback sendFileUpdateNotificationInterface = new SendFileUpdateNotificationInterface();
        downloadFiles.getFileFromServer(email, fileName, null, msg, sendFileNotificationInterface, sendFileUpdateNotificationInterface,
                mContext, mNotificationManager, mBuilder);
    }

    static class SendImageUpdateNotificationInterface implements MyActivity.Callback {
        @Override
        public void callBack(String position, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {

        }

        @Override
        public void callBack(String fileName, String msg, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {
            mBuilder.setProgress(0, 0, true);
            // Displays the progress bar for the first time.
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    static class SendFileNotificationInterface implements MyActivity.Callback {
        @Override
        public void callBack(String position, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {

        }

        @Override
        public void callBack(String fileName, String msg, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {
            String mimeType = MyActivity.getMimeType(fileName);
            String path = "file://" + Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
            Uri hacked_uri = Uri.parse(path);

            MediaScannerConnection.scanFile(context, new String[]{path}, null, null);

            Intent intentOpen = new Intent();
            intentOpen.setAction(Intent.ACTION_VIEW);
            intentOpen.setDataAndType(hacked_uri, mimeType);
            PendingIntent pendingOpen = PendingIntent.getActivity(mContext, 0, intentOpen, 0);

            Intent intentShare = new Intent();
            intentShare.setAction(Intent.ACTION_SEND);
            intentShare.putExtra(Intent.EXTRA_STREAM, hacked_uri);
            intentShare.setType("image/jpeg");
            PendingIntent pendingShare = PendingIntent.getActivity(mContext, 0, intentShare, 0);

            Intent intentFolder = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/");
            intentFolder.setDataAndType(hacked_uri, "text/csv");
            PendingIntent pendingFolder = PendingIntent.getActivity(mContext, 0, intentFolder, 0);

            Log.i(MyActivity.TAG, getClass().getName()+": "+ "Download file complete. Path: " + hacked_uri);

            mBuilder.setContentText("Download complete")
                    .addAction(R.drawable.action_folder, "Open Folder", pendingFolder)
                    .addAction(R.drawable.action_share, "Share", pendingShare)
                    .setContentIntent(pendingOpen)
                    .setProgress(0, 0, false);
            mNotificationManager.notify(1, mBuilder.build());
            Log.i(MyActivity.TAG,getClass().getName()+": "+ "Updating history. ");
            UpdateHistory updateHistory=new UpdateHistory();
            updateHistory.updateHistory(context);
        }
    }
    static class SendFileUpdateNotificationInterface implements MyActivity.Callback {
        @Override
        public void callBack(String position, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {

        }

        @Override
        public void callBack(String fileName, String msg, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {
            mBuilder.setProgress(100, Integer.parseInt(msg), false);
            // Displays the progress bar for the first time.
            mNotificationManager.notify(1, mBuilder.build());
        }
    }


}
