package luan.com.flippit.utilities;

import android.content.Context;

/**
 * Created by Luan on 2014-11-13.
 */
public abstract class HistoryInterface implements Callback {
    static Context context = null;

    public HistoryInterface(Context context) {
        this.context = context;
    }

    public HistoryInterface(Context context, int[] mAppWidgetIds) {

    }
}
