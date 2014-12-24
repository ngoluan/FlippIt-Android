package luan.com.flippit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MyActivity extends ActionBarActivity {
    public static final String TAG = "luan.com.pass";
    public static String CLASS_NAME = "";
    static FragmentManager mFragmentManager = null;
    static SharedPreferences mPrefs = null;
    static Context mContext;
    static android.support.v7.app.ActionBar mActionBar = null;
    static String regid;
    static String email = "";
    GoogleCloudMessaging gcm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set up global variables
        mContext = this;
        mFragmentManager = getSupportFragmentManager();
        mPrefs = getSharedPreferences(mContext.getPackageName(),
                Context.MODE_PRIVATE);

        setContentView(R.layout.activity_my);
        mActionBar = getSupportActionBar();
        email = mPrefs.getString("email", "");

        CLASS_NAME = mContext.getClass().getName();

        gcm = GoogleCloudMessaging.getInstance(mContext);
        if (GeneralUtilities.checkPlayServices(this)) {
            gcm = GoogleCloudMessaging.getInstance(this);

            regid = getRegistrationId(mContext);
            Log.i(MyActivity.TAG, getClass().getName() + ": " + "RegID: " + regid);
            if (regid.isEmpty() || regid.equals("")) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, mContext.getClass().getName() + ": " + "No valid Google Play Services APK found.");
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
        int currentVersion = GeneralUtilities.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }

        return registrationId;
    }

    private void registerInBackground() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {

                    regid = gcm.register("155899320902");
                    Log.d(TAG, "test" + regid.toString());

                    msg = "Device registered, registration ID=" + regid;
                    GeneralUtilities.storeRegistrationId(regid, mContext);
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
        mPrefs.edit().clear().commit();
        mFragmentManager.beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commit();
    }

    public void changeDeviceName() {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Change device name to:");
        dialog.setContentView(R.layout.dialog_devicename);
        dialog.show();

        Button saveButton = (Button) dialog.findViewById(R.id.saveButtonDialog);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText deviceName = (EditText) dialog.findViewById(R.id.deviceName);

                final SharedPreferences prefs = getSharedPreferences(mContext.getPackageName(),
                        Context.MODE_PRIVATE);
                int appVersion = GeneralUtilities.getAppVersion(mContext);
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
                        String line = "";
                        BufferedReader in = null;

                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = null;

                        httppost = new HttpPost(GeneralUtilities.SERVER_PATH + "server/changeDeviceName_v1.php");


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
                }.execute(deviceName.getText().toString());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            logout();
        } else if (id == R.id.action_refresh) {
            HistoryFragment.createListView(20);
        } else if (id == R.id.action_clear_all) {
            HistoryFragment.deleteHistoryAll();
        } else if (id == R.id.action_deviceName) {
            changeDeviceName();
        } else if (id == R.id.action_help) {
            getHelp();
        }
        return super.onOptionsItemSelected(item);
    }

    void getHelp() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        String[] choices = {"Sharing text tutorial", "Sharing file tutorial"};
        alertDialogBuilder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    GeneralUtilities.textTutorial(mContext);
                } else {
                    GeneralUtilities.fileTutorial(mContext);
                }
            }
        });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }
}
