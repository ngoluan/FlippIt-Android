package luan.com.pass.utilities;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import luan.com.pass.MyActivity;
import luan.com.pass.R;

interface UploadListener {
    public void onChange(int percent);
}

/**
 * Created by Luan on 2014-12-08.
 */
public class UploadFile2 implements Runnable, UploadListener {


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


    public UploadFile2(Context context, String filename, String email, String targetId, String sharedText, String targetType, Boolean saveMessage) {
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
        String line = "";
        BufferedReader in = null;

        try {
            File file = new File(filename);
            long totalSize = file.length();
            InputStream fileInputStream = new FileInputStream(file);

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            URL connectURL = new URL("http://local-motion.ca/pass/server/uploadSimple.php");

            // Open a HTTP connection to the URL
            HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();
            conn.setConnectTimeout(1000);
            // Allow Inputs
            conn.setDoInput(true);

            // Allow Outputs
            conn.setDoOutput(true);

            // Don't use a cached copy.
            conn.setUseCaches(false);

            // Use a post method.
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.addRequestProperty("Email", email);
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            CountingOutputStream dos = new CountingOutputStream(conn.getOutputStream(), this, totalSize, context);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"email\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(email);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"targetID\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(targetId);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"targetType\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(targetType);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"saveMessage\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(String.valueOf(saveMessage));
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            if (!sharedText.equals("")) {
                dos.writeBytes("Content-Disposition: form-data; name=\"message\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(sharedText);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
            }

            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + file.getName() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            // create a buffer of maximum size
            int bytesAvailable = fileInputStream.available();

            int sentBytes = 0;
            int maxBufferSize = 100 * 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            // read file and write it into form...
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                dos.flush();
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                sentBytes += bufferSize;
                float progress = ((float) sentBytes / (float) totalSize) * 100.0f;

            }
            Log.i(MyActivity.TAG, context.getClass().getName() + "Ending");
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            Log.i(MyActivity.TAG, context.getClass().getName() + "Closing file stream");
            fileInputStream.close();
            Log.i(MyActivity.TAG, context.getClass().getName() + "Flushing");
            dos.flush();
            Log.i(MyActivity.TAG, context.getClass().getName() + "Output stream close");
            dos.close();

            Log.i(MyActivity.TAG, context.getClass().getName() + "Getting input stream");
            InputStream is = conn.getInputStream();
            int ch;
            Log.i(MyActivity.TAG, context.getClass().getName() + "Getting message");
            StringBuffer b =
                    new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }

            String s = b.toString();
            Log.i(MyActivity.TAG, context.getClass().getName() + "Message" + s);
            line = s;

        } catch (ClientProtocolException e) {
            e.printStackTrace();
            // TODO Auto-generated catch block
        } catch (IOException e) {
            e.printStackTrace();
            // TODO Auto-generated catch block
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

class CountingOutputStream extends DataOutputStream {
    private long counter = 0l;
    private OutputStream outputStream;
    private UploadListener listener;
    private long length;
    private Context context = null;

    public CountingOutputStream(OutputStream out, UploadListener listener, long length, Context context) {
        super(out);
        this.outputStream = out;
        this.listener = listener;
        this.length = length;
    }

    /*    @Override
        public void write(int oneByte) throws IOException {
            this.outputStream.write(oneByte);
            counter++;
            if (listener != null) {
                int percent = (int) ((counter * 100)/ length);
                listener.onChange(percent);
            }
        }*/
    @Override
    public synchronized void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        incCount(len);
        counter++;
        if (listener != null) {
            int percent = (int) ((counter * 100) / length);
            listener.onChange(percent);
            Log.i(MyActivity.TAG, "Written: " + percent);
        }

    }

    private void incCount(int value) {
        int temp = written + value;
        if (temp < 0) {
            temp = Integer.MAX_VALUE;
        }
        written = temp;
    }
}