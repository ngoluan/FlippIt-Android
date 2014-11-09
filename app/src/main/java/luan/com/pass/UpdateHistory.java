package luan.com.pass;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by Luan on 2014-11-02.
 */
public class UpdateHistory {
    public static void updateHistory(Context context){
        try{
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningTaskInfo task : activityManager.getRunningTasks(Integer.MAX_VALUE)) {
                if ("luan.com.pass.MyActivity".equals(task.baseActivity.getClassName())) {
                    Fragment historyFragment = MyActivity.mFragmentManager.findFragmentByTag("historyFragment");
                    if (historyFragment.isVisible() == true) {
                        HistoryFragment.createListView(HistoryFragment.totalLoad);
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


}
