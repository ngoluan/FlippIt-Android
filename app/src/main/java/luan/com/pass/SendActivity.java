package luan.com.pass;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class SendActivity extends Activity {
    static final String TAG = "luan.com.pass";
    static public NotificationManager mNotificationManager;
    static CustomDeviceAdapter customDeviceAdapter = null;
    static String targetID = null;
    static String targetType = null;
    static SharedPreferences mPrefs = null;
    static ArrayList<DeviceItem> deviceItems = new ArrayList<DeviceItem>();
    static NotificationCompat.Builder mBuilder = null;
    static Context mContext = null;
    static String regID = null;
    GridView deviceGrid = null;

    static public void getDevices() {
        final String email = mPrefs.getString("email", "");
        regID = mPrefs.getString("registration_id", "");
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                // TODO Auto-generated method stub
                String result = postData();
                return result;
            }

            public String postData() {
                String line = "";
                BufferedReader in = null;

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://local-motion.ca/pass/getDevices.php");

                try {

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpclient.execute(httppost);

                    in = new BufferedReader(new InputStreamReader(
                            response.getEntity().getContent()));

                    line = in.readLine();

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
                return line;
            }

            @Override
            protected void onPostExecute(String msg) {

                try {
                    JSONArray devices = new JSONArray(msg);
                    deviceItems.clear();
                    for (int i = 0; i < devices.length(); i++) {
                        JSONObject device = devices.getJSONObject(i);
                        if (!device.getString("targetID").equals(regID)) {
                            deviceItems.add(new DeviceItem(device.getString("name"), device.getString("type"), device.getString("targetID")));
                        }
                    }
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("targetDevices", devices.toString());
                    editor.commit();

                    createListView();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    static  void createListView() {
        JSONArray devices = getStoredDevices();
        if (devices.length() == 0) {
            getDevices();
            return;
        }
        try {
            deviceItems.clear();
            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);
                if (!device.getString("targetID").equals(MyActivity.regid)) {
                    deviceItems.add(new DeviceItem(device.getString("name"), device.getString("type"), device.getString("targetID")));
                }
            }
            Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Devices received: " + String.valueOf(deviceItems.size()));
            deviceItems.add(0, new DeviceItem("cloud", "cloud", "cloud"));
            customDeviceAdapter.updateEntries(deviceItems);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray getStoredDevices() {

        String targetDevicesString = mPrefs.getString("targetDevices", "");
        Log.d(TAG, targetDevicesString);
        JSONArray deviceArray = new JSONArray();
        try {
            deviceArray = new JSONArray(targetDevicesString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return deviceArray;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mPrefs = getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        final String email = mPrefs.getString("email", "");

        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        final Dialog dialog = new Dialog(mContext, R.style.ThemeDialogCustom);
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_send);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialog.dismiss();
                finish();
            }
        });

        targetID = mPrefs.getString("targetID", "");
        targetType = mPrefs.getString("targetType", "");

        deviceGrid = (GridView) dialog.findViewById(R.id.gridView);
        customDeviceAdapter = new CustomDeviceAdapter(mContext);
        deviceGrid.setAdapter(customDeviceAdapter);

        ImageButton refreshButton = (ImageButton) dialog.findViewById(R.id.refreshDialog);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDevices();
            }
        });
        Button sendButton = (Button) dialog.findViewById(R.id.sendDialog);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Intent.ACTION_SEND.equals(action) && type != null) {
                    if ("text/plain".equals(type)) {
                        handleSendText(intent, email); // Handle text being sent
                    } else {
                        handleSendFile(intent, email); // Handle single image being sent
                    }

                }
            }
        });
        setContentView(R.layout.activity_send);
        createListView();
    }

    void handleSendText(Intent intent, final String email) {
        final String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            String targetID = null;
            String type = null;

            new AsyncTask<String, Integer, String>() {
                @Override
                protected String doInBackground(String... params) {
                    // TODO Auto-generated method stub
                    String result = postData();
                    return result;
                }

                public String postData() {
                    String line = "";
                    BufferedReader in = null;

                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = null;
                    if (targetType.equals("chrome")) {
                        httppost = new HttpPost("http://local-motion.ca/pass/sendChrome.php");
                    } else if (targetType.equals("android")) {
                        httppost = new HttpPost("http://local-motion.ca/pass/sendAndroid.php");
                    }

                    try {
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                        nameValuePairs.add(new BasicNameValuePair("email", email));
                        nameValuePairs.add(new BasicNameValuePair("targetID", SendActivity.targetID));
                        nameValuePairs.add(new BasicNameValuePair("message", sharedText));
                        nameValuePairs.add(new BasicNameValuePair("type", targetType));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                        HttpResponse response = httpclient.execute(httppost);

                        in = new BufferedReader(new InputStreamReader(
                                response.getEntity().getContent()));

                        line = in.readLine();

                    } catch (ClientProtocolException e) {
                        // TODO Auto-generated catch block
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                    }
                    return line;
                }

                @Override
                protected void onPostExecute(String msg) {
                    finish();
                }
            }.execute();
        }
    }

    void handleSendFile(Intent intent, final String email) {
        final Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
        String scheme = imageUri.getScheme();
        String filePath = null;
        if (scheme.equals("content")) {
            Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
            cursor.moveToFirst(); // <--no more NPE

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            filePath = cursor.getString(columnIndex);

            cursor.close();

        } else if (scheme.equals("file")) {
            filePath = imageUri.getPath();
        } else {
            Log.d(TAG, "Failed to load URI " + imageUri.toString());
        }
        final String filepath2 = filePath;

        mBuilder.setContentTitle("Pass")
                .setContentText("Sending...")
                .setSmallIcon(R.drawable.action_icon);

        if (imageUri != null) {
            String targetID = null;
            String type = null;
            new AsyncTask<String, Integer, String>() {
                @Override
                protected String doInBackground(String... params) {
                    // TODO Auto-generated method stub
                    String result = postData();
                    return result;
                }

                public String postData() {
                    String line = "";
                    BufferedReader in = null;

                    try {
                        File file = new File(filepath2);

                        InputStream fileInputStream = new FileInputStream(file);

                        String lineEnd = "\r\n";
                        String twoHyphens = "--";
                        String boundary = "*****";
                        URL connectURL = new URL("http://local-motion.ca/pass/upload.php");
                        Log.d(TAG, file.getName());

                        // Open a HTTP connection to the URL
                        HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();

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

                        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes("test");
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        dos.writeBytes("Content-Disposition: form-data; name=\"description\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes("testdesc");
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + file.getName() + "\"" + lineEnd);
                        dos.writeBytes(lineEnd);

                        // create a buffer of maximum size
                        int bytesAvailable = fileInputStream.available();
                        long totalSize = file.length();
                        int sentByes = 0;
                        int maxBufferSize = 1024;
                        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        byte[] buffer = new byte[bufferSize];

                        // read file and write it into form...
                        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {
                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                            sentByes += bufferSize;
                            float progress = ((float) sentByes / (float) totalSize) * 100.0f;
                            publishProgress((int) progress);
                        }
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                        // close streams
                        fileInputStream.close();

                        dos.flush();

                        Log.d(TAG, "File Sent, Response: " + String.valueOf(conn.getResponseCode()));

                        InputStream is = conn.getInputStream();

                        // retrieve the response from server
                        int ch;

                        StringBuffer b = new StringBuffer();
                        while ((ch = is.read()) != -1) {
                            b.append((char) ch);
                        }
                        String s = b.toString();
                        Log.d("Response", s);
                        dos.close();


                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = null;
                        if (targetType.equals("chrome")) {
                            httppost = new HttpPost("http://local-motion.ca/pass/sendChrome.php");
                        } else if (targetType.equals("android")) {
                            httppost = new HttpPost("http://local-motion.ca/pass/sendAndroid.php");
                        }

                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                        nameValuePairs.add(new BasicNameValuePair("email", email));
                        nameValuePairs.add(new BasicNameValuePair("fileName", s));
                        nameValuePairs.add(new BasicNameValuePair("targetID", SendActivity.targetID));
                        nameValuePairs.add(new BasicNameValuePair("message", "android send test"));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                        HttpResponse response = httpclient.execute(httppost);

                        in = new BufferedReader(new InputStreamReader(
                                response.getEntity().getContent()));

                        line = in.readLine();

                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                        // TODO Auto-generated catch block
                    } catch (IOException e) {
                        e.printStackTrace();
                        // TODO Auto-generated catch block
                    }
                    return line;
                }

                @Override
                protected void onPostExecute(String msg) {
                    mBuilder.setContentText("Send complete")
                            .setProgress(0, 0, false);
                    mNotificationManager.notify(1, mBuilder.build());
                    finish();
                }

                protected void onProgressUpdate(Integer... progress) {
                    mBuilder.setProgress(100, progress[0], false);
                    // Displays the progress bar for the first time.
                    mNotificationManager.notify(1, mBuilder.build());
                }
            }.execute();

        }
    }

    void deleteDevice(int position, final String email) {
        final String targetID = deviceItems.get(position).targetID;

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                // TODO Auto-generated method stub
                String result = postData();
                return result;
            }

            public String postData() {
                String line = "";
                BufferedReader in = null;

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = null;
                httppost = new HttpPost("http://local-motion.ca/pass/deleteDevice.php");

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("targetID", SendActivity.targetID));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpclient.execute(httppost);

                    in = new BufferedReader(new InputStreamReader(
                            response.getEntity().getContent()));

                    line = in.readLine();

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
                return line;
            }

            @Override
            protected void onPostExecute(String msg) {
                getDevices();
            }
        }.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
