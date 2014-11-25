package luan.com.pass.utilities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;

import luan.com.pass.DownloadFiles;
import luan.com.pass.GeneralUtilities;
import luan.com.pass.HistoryItem;
import luan.com.pass.MyActivity;

/**
 * Created by Luan on 2014-11-13.
 */
public class OpenFile {
    public OpenFile(HistoryItem historyItem, Context mContext){
            String fileName = historyItem.fileName;
            Uri hacked_uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
            String mimeType = GeneralUtilities.getMimeType(fileName);
        SharedPreferences mPrefs = mContext.getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
            String email = mPrefs.getString("email", "");
            DownloadFiles downloadFiles = new DownloadFiles();
            NotificationManager mNotificationManager = (NotificationManager)
                    mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            if (!file.exists()) {
                if (historyItem.type.equals("file")) {
                    Log.i(MyActivity.TAG, "Downloading file.");
                    MyActivity.Callback sendFileNotificationInterface = new SendFileNotificationInterface();
                    MyActivity.Callback sendFileUpdateNotificationInterface = new SendFileUpdateNotificationInterface();
                    downloadFiles.getFileFromServer(email, historyItem.fileName, null,
                            historyItem.message, sendFileNotificationInterface, sendFileUpdateNotificationInterface,
                            mContext, mNotificationManager, mBuilder);
                    return;
                } else if (historyItem.type.equals("image")) {
                    Log.i(MyActivity.TAG, "Downloading image.");
                    MyActivity.Callback sendImageNotificationInterface = new SendImageNotificationInterface();
                    MyActivity.Callback sendImageUpdateNotificationInterface = new SendImageUpdateNotificationInterface();
                    downloadFiles.getImageFromServer(email, historyItem.fileName, null,
                            historyItem.message, sendImageNotificationInterface, sendImageUpdateNotificationInterface,
                            mContext, mNotificationManager, mBuilder);
                    return;
                }
            }
            //Log.d(MyActivity.TAG, String.valueOf(position));
            Intent intentOpen = new Intent();
            intentOpen.setAction(Intent.ACTION_VIEW);
            intentOpen.setDataAndType(hacked_uri, mimeType);

            intentOpen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent new_intent = Intent.createChooser(intentOpen, "Pass");
            new_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.getApplicationContext().startActivity(new_intent);
    }

}
