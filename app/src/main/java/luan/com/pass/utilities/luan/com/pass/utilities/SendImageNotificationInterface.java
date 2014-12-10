package luan.com.pass.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import luan.com.pass.MyActivity;
import luan.com.pass.R;
import luan.com.pass.UpdateHistory;

/**
 * Created by Luan on 2014-11-02.
 */
public class SendImageNotificationInterface implements Callback {
    String TAG = null;


    public void callBack(String position, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {

    }


    public void callBack(String fileName, String msg, Bitmap image, Context context, NotificationManager notificationManager, NotificationCompat.Builder builder) {
        TAG = context.getPackageName();
        String path = null;
        OutputStream stream = null;

        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
            Log.i(TAG, getClass().getName() + ": " + "Creating file.");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            stream = new FileOutputStream(file);
            path = file.getPath();
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        MediaScannerConnection.scanFile(context, new String[]{path}, null, null);

        Intent intentOpen = new Intent();
        intentOpen.setAction(Intent.ACTION_VIEW);
        Uri hacked_uri = Uri.parse("file://" + path);
        intentOpen.setDataAndType(hacked_uri, "image/*");
        PendingIntent pendingOpen = PendingIntent.getActivity(context, 0, intentOpen, 0);

        Intent intentShare = new Intent();
        intentShare.setAction(Intent.ACTION_SEND);
        intentShare.putExtra(Intent.EXTRA_STREAM, hacked_uri);
        intentShare.setType("image/jpeg");
        PendingIntent pendingShare = PendingIntent.getActivity(context, 0, intentShare, 0);

        Intent intentFolder = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/");
        intentFolder.setDataAndType(uri, "text/csv");
        PendingIntent pendingFolder = PendingIntent.getActivity(context, 0, intentFolder, 0);

        Log.i(MyActivity.TAG, "Download file complete. Path: " + uri);

        builder.setContentTitle("Pass")
                .setContentText(msg)
                .setSmallIcon(R.drawable.action_icon)
                .setLargeIcon(image)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(image))
                .setContentIntent(pendingOpen)
                .addAction(R.drawable.action_folder, "Open Folder", pendingFolder)
                .addAction(R.drawable.action_share, "Share", pendingShare);


        Log.i(TAG, getClass().getName() + ": " + "Sending notification.");
        notificationManager.notify(1, builder.build());


        Log.i(TAG, getClass().getName() + ": " + "Updating history. ");
        UpdateHistory updateHistory = new UpdateHistory();
        updateHistory.updateHistory(context);
    }

    @Override
    public void callBackProgress(int progress) {

    }

    @Override
    public void callBackFinish(Bundle extras) {

    }
}