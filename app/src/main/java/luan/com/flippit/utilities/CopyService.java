package luan.com.flippit.utilities;

import android.app.IntentService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import luan.com.flippit.MyActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CopyService extends IntentService {
    Context mContext = null;

    public CopyService() {
        super("CopyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = this;
        Log.i(MyActivity.TAG, getClass().getName() + ": " + "Copy service");
        if (intent != null) {
            Log.i(MyActivity.TAG, getClass().getName() + ": " + "Copying: " + intent.getStringExtra("msg"));
            CopyClipboard(intent.getStringExtra("msg"), mContext);
        }
    }

    void CopyClipboard(String msg, Context mContext) {
        ClipboardManager clipboard = (ClipboardManager)
                mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Pass", msg);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(mContext, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        this.stopSelf();
    }
}
