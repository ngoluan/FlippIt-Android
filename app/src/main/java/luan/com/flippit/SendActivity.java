package luan.com.flippit;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import luan.com.flippit.utilities.Callback;
import luan.com.flippit.utilities.DecodeSampledBitmapFromPath;
import luan.com.flippit.utilities.HistoryInterface;
import luan.com.flippit.utilities.OnTaskCompleted;
import luan.com.flippit.utilities.UploadFile;


public class SendActivity extends Activity {
    static final String TAG = "luan.com.pass";
    private static final int REQUEST_CODE = 6384;
    //objects
    static public NotificationManager mNotificationManager;
    static SharedPreferences mPrefs = null;
    static NotificationCompat.Builder mBuilder = null;
    static CustomDeviceAdapter customDeviceAdapter = null;
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
    static boolean getDeviceAttempted = false;

    static public void getDevices() {
        getDeviceAttempted = true;
        Callback deviceCallback = new DeviceCallback(mContext);
        GeneralUtilities.getDevices(email, deviceCallback);
    }

    static void createCoverFlow() {

        JSONArray devices = getStoredDevices();
        Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Creating coverflow. Devices: " + devices.length());
        if (devices.length() <= 1 && getDeviceAttempted != true) {
            Toast.makeText(mContext, "Get the Chrome app from getchrome.flippit.ca.", Toast.LENGTH_LONG).show();
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

            customDeviceAdapter = new CustomDeviceAdapter(mContext);
            fancyCoverFlow.setAdapter(customDeviceAdapter);

            fancyCoverFlow.setUnselectedAlpha(1.0f);
            fancyCoverFlow.setUnselectedSaturation(0.0f);
            fancyCoverFlow.setUnselectedScale(0.5f);
            fancyCoverFlow.setSpacing(50);
            fancyCoverFlow.setMaxRotation(0);
            fancyCoverFlow.setScaleDownGravity(0.2f);
            fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);

            fancyCoverFlow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String targetID = customDeviceAdapter.deviceItems.get(position).targetID;
                    SendActivity.targetID = targetID;
                    String targetType = customDeviceAdapter.deviceItems.get(position).type;
                    SendActivity.targetType = targetType;
                    SharedPreferences.Editor editor = SendActivity.mPrefs.edit();
                    editor.putString("targetID", targetID);
                    editor.putString("targetType", targetType);
                    editor.commit();
                    CustomDeviceAdapter.updateEntries(deviceItems);
                    SendActivity.Send();
                }
            });
            fancyCoverFlow.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                    String[] choices = {"Change device name", "Delete device"};
                    alertDialogBuilder.setItems(choices, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                final Dialog dialog = new Dialog(mContext);
                                dialog.setTitle("Change device name to:");
                                dialog.setContentView(R.layout.dialog_devicename);
                                dialog.show();

                                Button saveButton = (Button) dialog.findViewById(R.id.saveButtonDialog);
                                saveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        EditText deviceName = (EditText) dialog.findViewById(R.id.deviceName);
                                        GeneralUtilities.changeDeviceName(deviceName.getText().toString(), deviceItems.get(position).targetID);
                                        dialog.dismiss();
                                    }
                                });
                            } else {
                                AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(mContext);

                                deleteDialogBuilder.setPositiveButton("Delete device?", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        SendActivity.deleteDevice(position, email);
                                        dialogInterface.dismiss();
                                    }
                                });
                                deleteDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                AlertDialog deleteDialog = deleteDialogBuilder.create();
                                deleteDialog.show();
                            }
                        }
                    });
                    AlertDialog dialog = alertDialogBuilder.create();
                    dialog.show();
                    return false;
                }
            });
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
                    httppost = new HttpPost(GeneralUtilities.SERVER_PATH + "server/send_v2.php");
                    Boolean localSaveMessage = false;
                    if (targetType.equals("cloud")) {
                        localSaveMessage = true;
                    } else {
                        localSaveMessage = saveMessage;
                    }
                    try {
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Sending text from " + email + " to " + targetID + " which is " + targetType + " of " + sharedText);
                        nameValuePairs.add(new BasicNameValuePair("email", email));
                        nameValuePairs.add(new BasicNameValuePair("targetID", targetID));
                        nameValuePairs.add(new BasicNameValuePair("message", sharedText));
                        nameValuePairs.add(new BasicNameValuePair("targetType", targetType));
                        nameValuePairs.add(new BasicNameValuePair("saveMessage", String.valueOf(localSaveMessage)));
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
                    filePath = "";
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

        Boolean localSaveMessage = false;
        if (targetType.equals("cloud")) {
            localSaveMessage = true;
        } else {
            localSaveMessage = saveMessage;
        }
        if (filePath.equals("")) {
            handleSendText(email); // Handle text being sent
        } else {
            EditText editText = (EditText) dialog
                    .findViewById(R.id.messageText);
            final String sharedText = editText.getText().toString();

            Thread t = new Thread(new UploadFile(mContext, filePath, email, targetID, sharedText, targetType, localSaveMessage));
            t.start();
            filePath = "";
            //handleSendFileTest(); // Handle single image being sent
        }
        ((Activity) mContext).finish();

    }

    static void deleteDevice(int position, final String email) {
        final String targetID = deviceItems.get(position).targetID;
        Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Delete device target: " + targetID);
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
                httppost = new HttpPost(GeneralUtilities.SERVER_PATH + "server/deleteDevice_v2.php");

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("targetID", targetID));
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
                Log.i(MyActivity.TAG, mContext.getClass().getName() + ": " + "Delete device query: " + msg);
                getDevices();
            }
        }.execute();

    }

    void setSave() {
        Button save = (Button) dialog.findViewById(R.id.saveButton);
        if (saveMessage == false) {
            saveMessage = true;
            save.setBackground(SendActivity.mContext.getResources().getDrawable(R.drawable.rounded_yellow));
            save.setTextColor(SendActivity.mContext.getResources().getColor(R.color.base_blue));
        } else {
            saveMessage = false;
            save.setBackground(SendActivity.mContext.getResources().getDrawable(R.drawable.rounded_blue));
            save.setTextColor(SendActivity.mContext.getResources().getColor(R.color.white));
        }
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean("saveMessage", saveMessage);
        editor.commit();

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
        if (savedInstanceState == null) {
            dialog = new Dialog(mContext, R.style.ThemeDialogCustom);
            dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_send);
            dialog.show();
        }

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
        saveMessage = mPrefs.getBoolean("saveMessage", false);
        if (saveMessage == true) {
            saveMessage = false;
        } else {
            saveMessage = true;
        }
        setSave();
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
        Button clearButton = (Button) dialog.findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) dialog
                        .findViewById(R.id.messageText);
                editText.setText("");
            }
        });
        ImageButton helpButton = (ImageButton) dialog.findViewById(R.id.helpDialog);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTutorial();
            }
        });
        setContentView(R.layout.activity_send);
        createCoverFlow();

        SharedPreferences mPrefs = mContext.getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);

        Boolean firstTimeSend = mPrefs.getBoolean("firstTimeSend", true);
        if (firstTimeSend == true) {
            showTutorial();
            firstTimeSend = false;
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean("firstTimeSend", firstTimeSend);
            editor.commit();
        }
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

    public void showTutorial() {

        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            private long time = 0;

            @Override
            public void run() {

                Toast.makeText(mContext, "First, type your message or select file you would like to share.", Toast.LENGTH_LONG).show();
                final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
                animation.setDuration(500); // duration - half a second
                animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                animation.setRepeatCount(5); // Repeat animation infinitely
                animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

                EditText editText = (EditText) dialog
                        .findViewById(R.id.messageText);
                editText.startAnimation(animation);


                final Handler i = new Handler();
                i.postDelayed(new Runnable() {
                    private long time = 0;

                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Then, tap on a device to share with.", Toast.LENGTH_LONG).show();
                        fancyCoverFlow.startAnimation(animation);
                    }
                }, 5000); // 1 second delay (takes millis)

            }
        }, 1500); // 1 second delay (takes millis)


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

    public static class DeviceCallback extends HistoryInterface {

        public DeviceCallback(Context context) {
            super(context);
        }

        @Override
        public void callBackProgress(int progress) {

        }

        @Override
        public void callBackFinish(Bundle extras) {
            String msg = extras.getString("msg");
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

        @Override
        public void callBackFinish(ArrayList<HistoryItem> historyItems) {

        }
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
