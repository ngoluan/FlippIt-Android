package luan.com.pass.utilities;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

import luan.com.pass.MyActivity;

/**
* Created by Luan on 2014-11-13.
*/
public class SendFileUpdateNotificationInterface implements MyActivity.Callback {
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
