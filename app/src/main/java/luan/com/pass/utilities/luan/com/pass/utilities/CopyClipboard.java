package luan.com.pass.utilities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by Luan on 2014-11-13.
 */
public class CopyClipboard {
    public CopyClipboard(String msg, Context mContext) {
        ClipboardManager clipboard = (ClipboardManager)
                mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Pass", msg);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(mContext, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
