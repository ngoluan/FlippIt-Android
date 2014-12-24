package luan.com.flippit;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Luan on 2014-11-02.
 */
public interface DownloadCallback {
    void callBack(String position, Bitmap image, Context context, NotificationManager notificationManager, NotificationCompat.Builder builder);

    void callBack(String fileName, String msg, Bitmap image, Context context, NotificationManager notificationManager, NotificationCompat.Builder builder);
}
