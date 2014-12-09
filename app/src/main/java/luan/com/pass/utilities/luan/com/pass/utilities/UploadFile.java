package luan.com.pass.utilities;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import luan.com.pass.MyActivity;
import luan.com.pass.R;

/**
 * Created by Luan on 2014-12-08.
 */
public class UploadFile implements Runnable, CountingInputStreamEntity.UploadListener {
    static Boolean saveMessage = false;
    Context context;
    String filename;
    String email;
    String targetId;
    String sharedText;
    String targetType;
    int lastPercent = 0;

    NotificationCompat.Builder mBuilder = null;
    NotificationManager mNotificationManager = null;

    public UploadFile(Context context, String filename, String email, String targetId, String sharedText, String targetType, Boolean saveMessage) {
        this.context = context;
        this.filename = filename;
        this.email = email;
        this.targetId = targetId;
        this.sharedText = sharedText;
        this.targetType = targetType;
        this.saveMessage = saveMessage;

        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Pass")
                .setContentText("Sending...")
                .setSmallIcon(R.drawable.action_icon);
    }

    @Override
    public void run() {
        final HttpResponse resp;
        final HttpClient httpClient = new DefaultHttpClient();
        File file = new File(filename);
        long totalSize = file.length();

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("targetID", targetId));
            nameValuePairs.add(new BasicNameValuePair("fileName", file.getName()));
            nameValuePairs.add(new BasicNameValuePair("message", sharedText));
            nameValuePairs.add(new BasicNameValuePair("targetType", targetType));
            nameValuePairs.add(new BasicNameValuePair("saveMessage", String.valueOf(saveMessage)));
            UrlEncodedFormEntity entity2 = new UrlEncodedFormEntity(nameValuePairs);
            String get = EntityUtils.toString(entity2);
            String url = "http://local-motion.ca/pass/server/upload_v2.php?" + get;

            Log.i(MyActivity.TAG, context.getClass().getName() + ": " + "Url: " + url);

            HttpPost post = new HttpPost(url);

            InputStream fileInputStream = new FileInputStream(file);
            CountingInputStreamEntity entity = new CountingInputStreamEntity(fileInputStream, totalSize);
            entity.setUploadListener(this);

            post.setEntity(entity);

            resp = httpClient.execute(post);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        resp.getEntity().getContent()));

                Log.i(MyActivity.TAG, context.getClass().getName() + ": " + "Message: " + in.readLine());
                String result = "";
                try {
                    JSONObject message = new JSONObject(in.readLine());
                    if (!message.optString("error", "").equals("")) {
                        result = message.getString("error");
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(result));
                    } else {
                        result = "Send complete";
                    }
                    mBuilder.setContentText(result);
                    mBuilder.setProgress(0, 0, false);
                    mNotificationManager.notify(1, mBuilder.build());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(MyActivity.TAG, context.getClass().getName() + ": " + "Error: " + resp.getStatusLine().getStatusCode());
            }
            resp.getEntity().consumeContent();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChange(int percent) {
        if (percent > lastPercent) {
            mBuilder.setProgress(100, percent, false);
            mNotificationManager.notify(1, mBuilder.build());
            lastPercent = percent;
        }
    }


}

class CountingInputStreamEntity extends InputStreamEntity {

    private UploadListener listener;
    private long length;

    public CountingInputStreamEntity(InputStream instream, long length) {
        super(instream, length);
        this.length = length;
    }

    public void setUploadListener(UploadListener listener) {
        this.listener = listener;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream));
    }

    public interface UploadListener {
        public void onChange(int percent);
    }

    class CountingOutputStream extends OutputStream {
        private long counter = 0l;
        private OutputStream outputStream;

        public CountingOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int oneByte) throws IOException {
            this.outputStream.write(oneByte);
            counter++;
            if (listener != null) {
                int percent = (int) ((counter * 100) / length);
                listener.onChange(percent);
            }
        }
    }

}