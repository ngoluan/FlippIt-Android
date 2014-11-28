package luan.com.pass;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

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

import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import luan.com.pass.utilities.DecodeSampledBitmapFromPath;
import luan.com.pass.utilities.OnTaskCompleted;


public class SendActivity extends Activity {
    static final String TAG = "luan.com.pass";
    private static final int REQUEST_CODE = 6384;
    //objects
    static public NotificationManager mNotificationManager;
    static SharedPreferences mPrefs = null;
    static NotificationCompat.Builder mBuilder = null;
    static FancyCoverFlow fancyCoverFlow;
    static Context mContext = null;
    static Application mApplication = null;
    static ArrayList<DeviceItem> deviceItems = new ArrayList<DeviceItem>();
    static Dialog dialog = null;
    static Intent mIntent = null;
    static String email = "";
    static String regID = null;
    static String targetID = null;
    static String targetType = null;
    static int folderLimit = 0;
    static String filePath = "";
    static Boolean saveMessage = false;
    static String message = "";

    static public void getDevices() {
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

                    createCoverFlow();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    static void createCoverFlow() {
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

            CustomDeviceAdapter customDeviceAdapter = new CustomDeviceAdapter();
            fancyCoverFlow.setAdapter(customDeviceAdapter);

            fancyCoverFlow.setUnselectedAlpha(1.0f);
            fancyCoverFlow.setUnselectedSaturation(0.0f);
            fancyCoverFlow.setUnselectedScale(0.5f);
            fancyCoverFlow.setSpacing(50);
            fancyCoverFlow.setMaxRotation(0);
            fancyCoverFlow.setScaleDownGravity(0.2f);
            fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
            customDeviceAdapter.updateEntries(deviceItems);
            for (int i = 0; i < deviceItems.size(); i++) {
                if (deviceItems.get(i).targetID.equals(SendActivity.targetID)) {
                    fancyCoverFlow.setSelection(i);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray getStoredDevices() {
        targetID = mPrefs.getString("targetID", "");
        targetType = mPrefs.getString("targetType", "");

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

    static void handleSendText(final String email) {
        EditText editText = (EditText) dialog
                .findViewById(R.id.messageText);
        final String sharedText = editText.getText().toString();
        if (sharedText != null) {
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
                    httppost = new HttpPost("http://local-motion.ca/pass/server/send_v1.php");

                    try {
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Sending text from " + email + " to " + targetID + " which is " + targetType + " of " + sharedText);
                        nameValuePairs.add(new BasicNameValuePair("email", email));
                        nameValuePairs.add(new BasicNameValuePair("targetID", targetID));
                        nameValuePairs.add(new BasicNameValuePair("message", sharedText));
                        nameValuePairs.add(new BasicNameValuePair("targetType", targetType));
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
                    Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Message " + msg);
                }
            }.execute();
        }
    }

    static void handleIntent() {
        String type = "";
        if (mIntent != null) {
            type = mIntent.getType();
            Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Type " + type);
            if ("text/plain".equals(type)) {
                String sharedText = mIntent.getStringExtra(Intent.EXTRA_TEXT);
                EditText editText = (EditText) dialog
                        .findViewById(R.id.messageText);
                editText.setText(sharedText);
            } else if (type != null) {
                final Uri uri = (Uri) mIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                String[] filePathColumn = {MediaStore.MediaColumns.DATA};
                String scheme = uri.getScheme();
                Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Scheme " + scheme);
                filePath = "";
                if (scheme.equals("content")) {
                    Cursor cursor = mContext.getContentResolver().query(uri, filePathColumn, null, null, null);
                    cursor.moveToFirst(); // <--no more NPE
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    filePath = cursor.getString(columnIndex);

                    cursor.close();
                    setImageView(filePath);
                    setFileTextView(filePath);
                } else if (scheme.equals("file")) {
                    filePath = uri.getPath();
                    if (type.contains("image") == true) {
                        setImageView(filePath);
                    }
                    setFileTextView(filePath);
                } else {
                    Log.d(TAG, "Failed to load URI " + mIntent.toString());
                }
            }
        }
    }

    static void setImageView(String path) {
        ImageView imageView = (ImageView) dialog
                .findViewById(R.id.uploadIimage);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap image = DecodeSampledBitmapFromPath.decodeSampledBitmapFromPath(path, 1000, 1000, options);
        imageView.setImageBitmap(image);
        imageView.setVisibility(View.VISIBLE);
    }

    static void setFileTextView(String path) {
        TextView textView = (TextView) dialog
                .findViewById(R.id.uploadFile);
        textView.setText(path);
        textView.setVisibility(View.VISIBLE);
    }

    static void Send() {
        createCoverFlow();

        if (filePath.equals("")) {
            handleSendText(email); // Handle text being sent
        } else {
            handleSendFile(email); // Handle single image being sent
        }
        //mApplication.finish();

    }

    static void handleSendFile(final String email) {
        EditText editText = (EditText) dialog
                .findViewById(R.id.messageText);
        final String sharedText = editText.getText().toString();

        mBuilder.setContentTitle("Pass")
                .setContentText("Sending...")
                .setSmallIcon(R.drawable.action_icon);

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
                    File file = new File(filePath);

                    InputStream fileInputStream = new FileInputStream(file);

                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String boundary = "*****";
                    URL connectURL = new URL("http://local-motion.ca/pass/server/upload_v1.php");

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
                    dos.writeBytes("Content-Disposition: form-data; name=\"email\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(email);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                    dos.writeBytes("Content-Disposition: form-data; name=\"targetID\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(targetID);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                    dos.writeBytes("Content-Disposition: form-data; name=\"targetType\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(targetType);
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


                    InputStream is = conn.getInputStream();

                    // retrieve the response from server
                    int ch;

                    StringBuffer b = new StringBuffer();
                    while ((ch = is.read()) != -1) {
                        b.append((char) ch);
                    }
                    String s = b.toString();
                    dos.close();

                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = null;
                    httppost = new HttpPost("http://local-motion.ca/pass/server/send_v1.php");

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("fileName", s));
                    nameValuePairs.add(new BasicNameValuePair("targetID", SendActivity.targetID));
                    nameValuePairs.add(new BasicNameValuePair("targetType", targetType));
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
                Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Message " + msg);
                mBuilder.setContentText("Send complete")
                        .setProgress(0, 0, false);
                mNotificationManager.notify(1, mBuilder.build());

            }

            protected void onProgressUpdate(Integer... progress) {
                mBuilder.setProgress(100, progress[0], false);
                // Displays the progress bar for the first time.
                mNotificationManager.notify(1, mBuilder.build());
            }
        }.execute();

    }

    void setSave() {
        Button save = (Button) dialog.findViewById(R.id.saveButton);
        if (saveMessage == false) {
            saveMessage = true;
            save.setBackground(SendActivity.mContext.getResources().getDrawable(R.drawable.rounded_yellow));
        } else {
            saveMessage = false;
            save.setBackground(SendActivity.mContext.getResources().getDrawable(R.drawable.rounded_blue));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mApplication = getApplication();
        mPrefs = getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        email = mPrefs.getString("email", "");
        regID = mPrefs.getString("registration_id", "");
        mIntent = getIntent();
        dialog = new Dialog(mContext, R.style.ThemeDialogCustom);
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
        handleIntent();
        targetID = mPrefs.getString("targetID", "");
        targetType = mPrefs.getString("targetType", "");

        fancyCoverFlow = (FancyCoverFlow) dialog.findViewById(R.id.fancyCoverFlow);

        ImageButton refreshButton = (ImageButton) dialog.findViewById(R.id.refreshDialog);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDevices();
            }
        });
        Button fileButton = (Button) dialog.findViewById(R.id.file);
        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooser();
            }
        });
        Button saveButton = (Button) dialog.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSave();
            }
        });
        setContentView(R.layout.activity_send);
        createCoverFlow();
    }

    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, "Choose your file");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        final Uri uri = data.getData();
                        try {
                            // Get the file path from the URI
                            filePath = FileUtils.getPath(this, uri);
                            Toast.makeText(mContext,
                                    "File Selected: " + filePath, Toast.LENGTH_LONG).show();
                            String type = GeneralUtilities.getMimeType(filePath);
                            if (type.contains("image") == true) {
                                setImageView(filePath);
                                setFileTextView(filePath);
                            }
                        } catch (Exception e) {
                            Log.e("FileSelectorTestActivity", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

class FolderSizeCallback implements OnTaskCompleted {
    @Override
    public void onTaskCompleted(int folderSize, Context context) {
        if (folderSize > 10 * 1024) {
            int limit = folderSize / (10 * 1024) * 100;
            SendActivity.folderLimit = limit;
        }
    }
}
