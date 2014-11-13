package luan.com.pass.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import luan.com.pass.HistoryItem;
import luan.com.pass.MyActivity;

/**
 * Created by Luan on 2014-11-13.
 */
public class ShareItem {
    public ShareItem(HistoryItem historyItem, Context mContext) {
        Intent shareIntent = new Intent();
        if (historyItem.type.equals("text")) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, historyItem.message);
            shareIntent.setType("text/plain");
        } else {
            String fileName = historyItem.fileName;
            Uri hacked_uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
            String mimeType = MyActivity.getMimeType(fileName);

            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, hacked_uri);
            shareIntent.setType(mimeType);
        }

        mContext.startActivity(Intent.createChooser(shareIntent, "Pass"));
    }
}
