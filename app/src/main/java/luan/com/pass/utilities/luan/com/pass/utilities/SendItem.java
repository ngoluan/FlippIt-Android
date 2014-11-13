package luan.com.pass.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import luan.com.pass.HistoryItem;
import luan.com.pass.MyActivity;
import luan.com.pass.SendActivity;

/**
 * Created by Luan on 2014-11-13.
 */
public class SendItem {
    public SendItem(HistoryItem historyItem, Context mContext) {
        Intent sendIntent = new Intent(mContext, SendActivity.class);
        if (historyItem.type.equals("text")) {
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, historyItem.message);
            sendIntent.setType("text/plain");
        } else {
            String fileName = historyItem.fileName;
            Uri hacked_uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
            String mimeType = MyActivity.getMimeType(fileName);

            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, hacked_uri);
            sendIntent.setType(mimeType);
        }

        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mContext.startActivity(sendIntent);
    }
}
