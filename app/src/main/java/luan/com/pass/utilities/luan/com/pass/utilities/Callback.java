package luan.com.pass.utilities;

import android.os.Bundle;

import java.util.ArrayList;

import luan.com.pass.HistoryItem;

/**
 * Created by Luan on 2014-12-09.
 */
public interface Callback {
    //void callBack(String position, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder);

    void callBackProgress(int progress);

    void callBackFinish(Bundle extras);

    void callBackFinish(ArrayList<HistoryItem> historyItems);
}
