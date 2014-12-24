package luan.com.flippit.utilities;

import java.util.ArrayList;

import luan.com.flippit.HistoryItem;

/**
 * Created by Luan on 2014-11-11.
 */
public interface HistoryGetCallbackInterface {
    void callBack(ArrayList<HistoryItem> historyItems);

    void callBack(int i);
}
