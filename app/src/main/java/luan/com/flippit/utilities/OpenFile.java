package luan.com.flippit.utilities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;

import luan.com.flippit.DownloadFiles;
import luan.com.flippit.GeneralUtilities;
import luan.com.flippit.HistoryItem;
import luan.com.flippit.MyActivity;
import luan.com.flippit.R;

/**
 * Created by Luan on 2014-11-13.
 */
public class OpenFile {
    public OpenFile(HistoryItem historyItem, Context mContext) {
        String fileName = historyItem.fileName;
        String msgId = String.valueOf(historyItem.dbID);
        Uri hacked_uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
        String mimeType = GeneralUtilities.getMimeType(fileName);
        SharedPreferences mPrefs = mContext.getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
        String email = mPrefs.getString("email", "");
        NotificationManager mNotificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);

        if (!file.exists()) {
            Log.i(MyActivity.TAG, getClass().getName() + ": " + "File transfer.");

            mBuilder.setContentTitle("FlippIt")
                    .setContentText("Downloading...")
                    .setSmallIcon(R.drawable.action_icon);

            String url = GeneralUtilities.SERVER_PATH + "uploads/" + email + "/" + fileName;

            Bundle extras = new Bundle();
            extras.putString("email", email);
            extras.putString("filename", fileName);
            extras.putString("msgId", msgId);

            DownloadFiles downloadFiles = new DownloadFiles(mContext);
            Callback fileCallback = new FileCallback(mContext);
            downloadFiles.getFileFromServer_v2(url, extras, fileCallback);
        }

        Intent intentOpen = new Intent();
        intentOpen.setAction(Intent.ACTION_VIEW);
        intentOpen.setDataAndType(hacked_uri, mimeType);

        intentOpen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent new_intent = Intent.createChooser(intentOpen, "FlippIt");
        new_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.getApplicationContext().startActivity(new_intent);
    }

}
