package luan.com.pass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

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

/**
 * Created by Luan on 2014-11-24.
 */
public class GeneralUtilities {
    static public final String TAG = "luan.com.pass";
    static public final String SERVER_PATH = "http://www.flippit.ca/";
    static public final String SAVE_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS) + "/";
    static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static final int FOLDER_LIMIT = 50 * 1024;

    static int getAppVersion(Context context) {
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
        fileName = fileName.toLowerCase();

        if (fileName.contains(".jpg") == true || fileName.contains(".jpeg") == true || fileName.contains(".gif") == true || fileName.contains(".png") == true) {
            type = "image";
        } else if (!fileName.equals("null") && !fileName.isEmpty()) {
            type = "file";
        } else {
            type = "text";
        }

        return type;
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

    static boolean checkPlayServices(MyActivity myActivity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(myActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, myActivity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(MyActivity.mContext, "This device is not supported.", Toast.LENGTH_SHORT).show();
                //finish();
            }
            return false;
        }
        return true;
    }

    static void storeRegistrationId(String regId, Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(MyActivity.mContext.getPackageName(),
                Context.MODE_PRIVATE);
        int appVersion = getAppVersion(MyActivity.mContext);
        Log.i(MyActivity.TAG, "Saving regId on app version " + appVersion);
        Log.i(MyActivity.TAG, "Saving regId on app version " + regId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("registration_id", regId);
        editor.putInt("appVersion", appVersion);
        editor.commit();
    }

    static void signalMessageReceived(final String msgId, final String email, final Context context) {

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
                HttpPost httppost = new HttpPost("http://local-motion.ca/pass/server/messageViewed_v1.php");

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("messageId", msgId));
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
                Log.i(MyActivity.TAG, getClass().getName() + ": " + "Delete server message: " + msg);
            }
        }.execute();

    }

    public static void textTutorial(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://m6S2m-vS204"));
        context.startActivity(intent);
    }

    public static void fileTutorial(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://zyfscyL1gJU"));
        context.startActivity(intent);
    }
    public static NotificationCompat.Builder createNotificationBuilder(Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Pass");
        return mBuilder;
    }
}
