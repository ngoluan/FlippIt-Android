package luan.com.pass.widget;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

import luan.com.pass.HistoryFragment;
import luan.com.pass.HistoryItem;
import luan.com.pass.R;

/**
 * Created by Luan on 2014-11-10.
 */
public class CustomWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return(new ListViewFactory(this.getApplicationContext()));
    }
}
class ListViewFactory implements RemoteViewsService.RemoteViewsFactory{
    private Context mContext =null;
    private int appWidgetId;
    public ListViewFactory(Context ctxt) {
        this.mContext =ctxt;
    }
    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews row=null;
        row=new RemoteViews(mContext.getPackageName(), R.layout.row_history);

        row.setTextViewText(R.id.dateTime, WidgetProvider.historyItems.get(i).dateTime);
        if(WidgetProvider.historyItems.get(i).type.equals("text")){
            row.setTextViewText(R.id.message,WidgetProvider.historyItems.get(i).message);
            row.setViewVisibility(R.id.open, View.GONE);
        }
        else{
            if (WidgetProvider.historyItems.get(i).type.equals("file")) {
                row.setTextViewText(R.id.message,"File transfer: " + WidgetProvider.historyItems.get(i).fileName );
            } else if (WidgetProvider.historyItems.get(i).type.equals("image")) {
                if (WidgetProvider.historyItems.get(i).bitmap == null) {
                    row.setTextViewText(R.id.message,"Image transfer: " + WidgetProvider.historyItems.get(i).fileName +"\nImage not available on device. Tap to download.");
                }
            }
            /*if(!WidgetProvider.historyItems.get(i).message.equals("")){//attaches message to file or image transfer if a message exist
                row.setTextViewText(R.id.message,messageText.getText().toString()+ "\n"+ WidgetProvider.historyItems.get(i).message)   ;
            }
            if(messageText.getText().toString().indexOf("\n")==0){//trims the new line character if message begins one. could happen if image posted without warning that you need to download it
                row.setTextViewText(R.id.message,messageText.getText().toString().substring(1));
            }
            row.setViewVisibility(R.id.copy, View.GONE);*/
        }

        return null;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
