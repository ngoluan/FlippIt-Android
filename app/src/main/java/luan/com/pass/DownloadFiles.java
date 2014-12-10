package luan.com.pass;

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

import luan.com.pass.utilities.Callback;

/**
 * Created by Luan on 2014-10-30.
 */
public class DownloadFiles {
    static Context context = null;

    public DownloadFiles(Context context) {
        this.context = context;
    }

    /*static public void getImageFromServer(final String email, final String fileName, final String position,
                                          final String msg, final Callback mainCallback, final Callback updateCallback,
                                          final Context context, final NotificationManager mNotificationManager, final NotificationCompat.Builder mBuilder) {
        mBuilder.setContentTitle("Pass")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.action_icon);
        Toast.makeText(context, "Downloading file.", Toast.LENGTH_SHORT).show();
        new AsyncTask<String, String, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                // TODO Auto-generated method stub
                Bitmap result = postData(params[0]);
                return result;
            }

            public Bitmap postData(String url) {
                InputStream in = null;
                Bitmap image = null;
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();

                HttpPost httppost = null;

                try {
                    if (fileName.contains("http") == true) {
                        url = fileName;
                    } else {
                        url = "http://www.local-motion.ca/pass/uploads/" + email + '/' + URLEncoder.encode(url, "UTF-8");
                    }
                    Log.i(MyActivity.TAG, getClass().getName() + ": " + "Getting image. Url: " + url);
                    httppost = new HttpPost(url);
                    HttpResponse response = httpclient.execute(httppost);

                    in = response.getEntity().getContent();

                    image = BitmapFactory.decodeStream(in);
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
                return image;
            }

            @Override
            protected void onPostExecute(Bitmap image) {
                Log.i(MyActivity.TAG, getClass().getName() + ": " + "Image received. Filesize: " + image.getByteCount());
                mainCallback.callBack(position, image, context, mNotificationManager, mBuilder);
                mainCallback.callBack(fileName, msg, image, context, mNotificationManager, mBuilder);
            }

            protected void onProgressUpdate(String... progress) {

                updateCallback.callBack(null, progress[0], null, context, mNotificationManager, mBuilder);
            }
        }.execute(fileName);
    }

    static public void getFileFromServer(final String email, final String fileName, final String position, final String msg,
                                         final Callback mainCallback, final Callback updateCallback,
                                         final Context context, final NotificationManager mNotificationManager, final NotificationCompat.Builder mBuilder) {
        mBuilder.setContentTitle("Pass")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.action_icon);
        Toast.makeText(context, "Downloading file.", Toast.LENGTH_SHORT).show();
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                int count;
                String path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
                try {
                    URI uri = new URI("http",
                            "www.local-motion.ca",
                            "/pass/uploads/" + email + "/" + fileName,
                            null);
                    String url = uri.toString();

                    HttpGet httpGet = new HttpGet(url);
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpResponse response = httpclient.execute(httpGet);
                    InputStream content = response.getEntity().getContent();

                    // this will be useful so that you can show a tipical 0-100% progress bar
                    int lengthOfFile = (int) response.getEntity().getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(content, 8192);

                    // Output stream
                    OutputStream output = new FileOutputStream(path);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress("" + (int) ((total * 100) / lengthOfFile));

                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return fileName;
            }

            @Override
            protected void onPostExecute(String fileName) {
                Log.i(MyActivity.TAG, mainCallback.toString());
                mainCallback.callBack(fileName, null, null, context, mNotificationManager, mBuilder);
            }

            protected void onProgressUpdate(String... progress) {
                updateCallback.callBack(null, progress[0], null, context, mNotificationManager, mBuilder);
            }

        }.execute();
    }*/
    static public void getFileFromServer_v2(final String url, final Bundle extras, final Callback callback) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Pass")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.action_icon)
                .setProgress(100, 0, false);
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

        final String filename = extras.getString("filename");
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

                callback.callBackFinish(extras);
            }

            protected void onProgressUpdate(Integer... progress) {
                callback.callBackProgress(progress[0]);
            }

        }.execute();
    }
}
