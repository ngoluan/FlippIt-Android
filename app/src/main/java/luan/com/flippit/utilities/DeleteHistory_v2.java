package luan.com.flippit.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

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

import luan.com.flippit.GeneralUtilities;
import luan.com.flippit.MyActivity;

/**
 * Created by Luan on 2014-11-13.
 */
public class DeleteHistory_v2 {
    public DeleteHistory_v2(final int id, final int position, Context context, final Callback callback) {
        final SharedPreferences mPrefs = context.getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE);
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
                HttpPost httppost = new HttpPost(GeneralUtilities.SERVER_PATH + "server/deleteMessage_v1.php");
                String email = mPrefs.getString("email", "");
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    Log.i(MyActivity.TAG, getClass().getName() + ": " + "Server post: " + String.valueOf(id) + email);
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
                Bundle extras = new Bundle();
                extras.putInt("position", position);
                callback.callBackFinish(extras);
            }
        }.execute();
    }
}
