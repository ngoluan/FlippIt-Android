package luan.com.pass;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class MyActivity extends ActionBarActivity {
    static final String TAG = "luan.com.pass";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static FragmentManager mFragmentManager = null;
    static SharedPreferences mPrefs = null;
    static Context mContext;
    static String regid;
    GoogleCloudMessaging gcm;

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    static public String typeOfMessage(String fileName) {
        String type = null;
        fileName=fileName.toLowerCase();
        if (fileName.contains(".jpg") == true || fileName.contains(".jpeg") == true || fileName.contains(".gif") == true || fileName.contains(".png") == true) {
            type= "image";
        } else if (!fileName.equals("")) {
            type= "file";
        } else {
            type= "text";
        }
        Log.i(MyActivity.TAG, "Message type: " + type);
        return type;
    }



    static public void copyClipboard(String msg) {
        ClipboardManager clipboard = (ClipboardManager)
                MyActivity.mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Pass", msg);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MyActivity.mContext, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    static public String getMimeType(String fileName) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(fileExt(fileName).substring(1));
        return mimeType;
    }

    static public String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf("."));
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set up global variables
        mContext = this;
        mFragmentManager = getSupportFragmentManager();
        mPrefs = getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        gcm = GoogleCloudMessaging.getInstance(mContext);
        setContentView(R.layout.activity_my);

        String email = mPrefs.getString("email", "");

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);

            regid = getRegistrationId(mContext);
            Log.d(TAG,"test1"+regid);
            if (regid.isEmpty()|| regid.equals("")) {

                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        if (savedInstanceState == null) {
            Fragment fragment = null;
            String tag = null;

            //loginFragment if not logged in
            if (email.equals("")) {
                fragment = new LoginFragment();
                tag = "loginFragment";
            } else {
                fragment = new HistoryFragment();
                tag = "historyFragment";
            }
            mFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, tag)
                    .commit();
        }

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(mContext, "This device is not supported.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void storeRegistrationId(String regId) {
        final SharedPreferences prefs = getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);
        int appVersion = getAppVersion(mContext);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        Log.i(TAG, "Saving regId on app version " + regId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("registration_id", regId);
        editor.putInt("appVersion", appVersion);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE);
        String registrationId = prefs.getString("registration_id", "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt("appVersion", Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }

        return registrationId;
    }

    private void registerInBackground() {
/*        String msg = null;
        try {
            Log.d(TAG,"test"+gcm.toString());
            regid = gcm.register("155379597538");
            Log.d(TAG,"test"+regid.toString());
            msg = "Device registered, registration ID=" + regid;
            storeRegistrationId(regid);
            Log.d(TAG, regid);
        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
        }*/
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    Log.d(TAG,"test"+gcm.toString());
                    regid = gcm.register("155379597538");
                    Log.d(TAG,"test"+regid.toString());
                    msg = "Device registered, registration ID=" + regid;
                    storeRegistrationId(regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
            }
        }.execute(null, null, null);
    }

    public void logout() {
        mPrefs.edit().clear();
        mFragmentManager.beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commit();
    }
    public void changeDeviceName(){
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Change device name to:");
        dialog.setContentView(R.layout.dialog_devicename);
        dialog.show();

        Button saveButton = (Button) dialog.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText deviceName = (EditText)dialog.findViewById(R.id.deviceName);

                final SharedPreferences prefs = getSharedPreferences(mContext.getPackageName(),
                        Context.MODE_PRIVATE);
                int appVersion = getAppVersion(mContext);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("deviceName", deviceName.getText().toString());
                editor.commit();

                //getSupportActionBar().setTitle(deviceName.getText().toString());

                new AsyncTask<String, Integer, String>() {
                    @Override
                    protected String doInBackground(String... params) {
                        // TODO Auto-generated method stub
                        String result = postData(params[0]);
                        return result;
                    }
                    public String postData(String deviceName2) {
                        String line ="";
                        BufferedReader in = null;

                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = null;

                        httppost = new HttpPost("http://local-motion.ca/pass/changeDeviceName.php");


                        try {
                            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                            nameValuePairs.add(new BasicNameValuePair("targetID", regid));
                            nameValuePairs.add(new BasicNameValuePair("deviceName", deviceName2));
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
                        dialog.dismiss();
                    }
                }.execute( deviceName.getText().toString());
            }
        });
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
        else if (id == R.id.action_logout) {
            logout();
        }
        else if (id == R.id.action_refresh) {
            HistoryFragment.createListView(20);
        }
        else if (id == R.id.action_clear_all) {
            HistoryFragment.deleteHistoryAll();
        }
        else if (id == R.id.action_deviceName) {
            changeDeviceName();
        }
            return super.onOptionsItemSelected(item);
    }

    public interface Callback {
        void callBack(String position, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder);

        void callBack(String fileName, String msg, Bitmap image, Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder);
    }
}
