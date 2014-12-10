package luan.com.pass.utilities;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import luan.com.pass.R;

/**
 * Created by Luan on 2014-11-13.
 */
public abstract class DownloadInterface implements Callback {
    Context context = null;
    NotificationManager mNotificationManager = null;
    NotificationCompat.Builder mBuilder = null;

    public DownloadInterface(Context context) {
        this.context = context;
        this.mNotificationManager = (NotificationManager)
                this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this.context);
        mBuilder.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Pass");
    }
}
