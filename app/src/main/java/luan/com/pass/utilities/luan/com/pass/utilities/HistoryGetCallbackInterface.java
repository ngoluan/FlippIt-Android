package luan.com.pass.utilities;

import java.util.ArrayList;

import luan.com.pass.HistoryItem;

/**
 * Created by Luan on 2014-11-11.
 */
public interface HistoryGetCallbackInterface {
    void callBack(ArrayList<HistoryItem> historyItems);
    void callBack(int i);
}
