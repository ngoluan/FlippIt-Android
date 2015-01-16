package luan.com.flippit;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import luan.com.flippit.utilities.Callback;

/**
 * Created by Luan on 2014-10-30.
 */
public class DownloadFiles {
    static Context context = null;

    public DownloadFiles(Context context) {
        this.context = context;
    }

    static public void getFileFromServer_v2(final String url, final Bundle extras, final Callback callback) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("FlippIt")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.action_icon)
                .setProgress(100, 0, false);
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

        final String filename = extras.getString("filename");
        final String email = extras.getString("email");
        final String msgId = extras.getString("msgId");
        final String msg = extras.getString("msg");

        final String savePath = GeneralUtilities.SAVE_PATH + filename;
        Log.i(MyActivity.TAG, context.getClass().getName() + ": " + "Downloading file from: " + url);
        Toast.makeText(context, "Downloading file.", Toast.LENGTH_SHORT).show();

        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                int count;
                try {

                    HttpGet httpGet = new HttpGet(url);
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpResponse response = httpclient.execute(httpGet);
                    InputStream content = response.getEntity().getContent();

                    int lengthOfFile = (int) response.getEntity().getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(content, 8192);

                    // Output stream
                    OutputStream output = new FileOutputStream(savePath);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress((int) ((total * 100) / lengthOfFile));
                        output.write(data, 0, count);
                    }

                    Log.i(MyActivity.TAG, context.getClass().getName() + ": " + "Download complete. Saved file at: " + savePath);

                    output.flush();
                    output.close();
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Bundle extras = new Bundle();
                extras.putString("msg", msg);
                extras.putString("filename", filename);
                extras.putString("savedPath", savePath);
                GeneralUtilities.signalMessageReceived(msgId, email, context);
                callback.callBackFinish(extras);
            }

            protected void onProgressUpdate(Integer... progress) {
                callback.callBackProgress(progress[0]);
            }

        }.execute();
    }
}
