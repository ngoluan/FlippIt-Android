package luan.com.pass;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by Luan on 2014-11-24.
 */
public class GeneralUtilities {
    static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

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
        } else if (!fileName.equals("")) {
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
}
