package luan.com.flippit.utilities;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import luan.com.flippit.GeneralUtilities;
import luan.com.flippit.MyActivity;
import luan.com.flippit.R;

/**
 * Created by Luan on 2014-12-08.
 */
public class UploadFile implements Runnable {
    static Boolean saveMessage = false;
    static long totalSize;
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

    public static String getContent(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String body = "";
        String content = "";

        while ((body = rd.readLine()) != null) {
            content += body + "\n";
        }
        return content.trim();
    }

    @Override
    public void run() {
        File file = new File(filename);
        totalSize = file.length();


        String responseString = "no";

        File sourceFile = new File(filename);
        if (!sourceFile.isFile()) {

        }


        try {


            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(GeneralUtilities.SERVER_PATH + "server/upload_v2.php");
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            FileBody fb = new FileBody(sourceFile);

            Log.i(MyActivity.TAG, getClass().getName() + ": " + "Sending file from " + email + " to " + targetId + " which is " + targetType + " of " + sharedText);

            builder.addPart("file", fb);
            builder.addTextBody("email", email);
            builder.addTextBody("targetID", targetId);
            builder.addTextBody("targetType", targetType);
            builder.addTextBody("message", sharedText);
            builder.addTextBody("saveMessage", String.valueOf(saveMessage));
            final HttpEntity yourEntity = builder.build();

            class ProgressiveEntity implements HttpEntity {
                @Override
                public void consumeContent() throws IOException {
                    yourEntity.consumeContent();
                }

                @Override
                public InputStream getContent() throws IOException,
                        IllegalStateException {
                    return yourEntity.getContent();
                }

                @Override
                public Header getContentEncoding() {
                    return yourEntity.getContentEncoding();
                }

                @Override
                public long getContentLength() {
                    return yourEntity.getContentLength();
                }

                @Override
                public Header getContentType() {
                    return yourEntity.getContentType();
                }

                @Override
                public boolean isChunked() {
                    return yourEntity.isChunked();
                }

                @Override
                public boolean isRepeatable() {
                    return yourEntity.isRepeatable();
                }

                @Override
                public boolean isStreaming() {
                    return yourEntity.isStreaming();
                } // CONSIDER put a _real_ delegator into here!

                @Override
                public void writeTo(OutputStream outstream) throws IOException {

                    class ProxyOutputStream extends FilterOutputStream {
                        /**
                         * @author Stephen Colebourne
                         */

                        public ProxyOutputStream(OutputStream proxy) {
                            super(proxy);
                        }

                        public void write(int idx) throws IOException {
                            out.write(idx);
                        }

                        public void write(byte[] bts) throws IOException {
                            out.write(bts);
                        }

                        public void write(byte[] bts, int st, int end) throws IOException {
                            out.write(bts, st, end);
                        }

                        public void flush() throws IOException {
                            out.flush();
                        }

                        public void close() throws IOException {
                            out.close();
                        }
                    } // CONSIDER import this class (and risk more Jar File Hell)

                    class ProgressiveOutputStream extends ProxyOutputStream {
                        long totalSent;

                        public ProgressiveOutputStream(OutputStream proxy) {
                            super(proxy);
                            totalSent = 0;
                        }

                        public void write(byte[] bts, int st, int end) throws IOException {

                            totalSent += end;
                            publishProgress((int) ((totalSent / (float) totalSize) * 100));

                            out.write(bts, st, end);
                        }
                    }

                    yourEntity.writeTo(new ProgressiveOutputStream(outstream));
                }

            }
            ;
            ProgressiveEntity myEntity = new ProgressiveEntity();

            post.setEntity(myEntity);
            HttpResponse response = client.execute(post);
            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            String msg = in.readLine();
            Log.i(MyActivity.TAG, context.getClass().getName() + ": " + "Response: " + msg);

            String result = "";
            try {
                JSONObject message = new JSONObject(msg);
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

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void publishProgress(int percent) {
        if (percent > lastPercent) {
            mBuilder.setProgress(100, percent, false);
            mNotificationManager.notify(1, mBuilder.build());
            lastPercent = percent;
        }
    }


}

