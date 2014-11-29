package luan.com.pass.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import luan.com.pass.GeneralUtilities;
import luan.com.pass.MyActivity;
import luan.com.pass.R;
import luan.com.pass.UpdateHistory;

/**
 * Created by Luan on 2014-11-13.
 */
public class SendFileNotificationInterface implements MyActivity.Callback {
    @Override
    public void callBack(String position, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {

    }

    @Override
    public void callBack(String fileName, String msg, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {
        String mimeType = GeneralUtilities.getMimeType(fileName);
        String path = "file://" + Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
        Uri hacked_uri = Uri.parse(path);

        MediaScannerConnection.scanFile(context, new String[]{path}, null, null);

        Intent intentOpen = new Intent();
        intentOpen.setAction(Intent.ACTION_VIEW);
        intentOpen.setDataAndType(hacked_uri, mimeType);
        PendingIntent pendingOpen = PendingIntent.getActivity(context, 0, intentOpen, 0);

        Intent intentShare = new Intent();
        intentShare.setAction(Intent.ACTION_SEND);
        intentShare.putExtra(Intent.EXTRA_STREAM, hacked_uri);
        intentShare.setType("image/jpeg");
        PendingIntent pendingShare = PendingIntent.getActivity(context, 0, intentShare, 0);

        Intent intentFolder = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/");
        intentFolder.setDataAndType(hacked_uri, "text/csv");
        PendingIntent pendingFolder = PendingIntent.getActivity(context, 0, intentFolder, 0);

        Log.i(MyActivity.TAG, getClass().getName() + ": " + "Download file complete. Path: " + hacked_uri);

        mBuilder.setContentText("Download complete")
                .addAction(R.drawable.action_folder, "Open Folder", pendingFolder)
                .addAction(R.drawable.action_share, "Share", pendingShare)
                .setContentIntent(pendingOpen)
                .setProgress(0, 0, false);
        mNotificationManager.notify(1, mBuilder.build());
        Log.i(MyActivity.TAG, getClass().getName() + ": " + "Updating history. ");
        UpdateHistory updateHistory = new UpdateHistory();
        updateHistory.updateHistory(context);
    }
}
