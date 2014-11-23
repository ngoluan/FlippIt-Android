package luan.com.pass.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import luan.com.pass.MyActivity;

/**
 * Created by Luan on 2014-11-22.
 */
public class GetFolderSize {
    public static int folderSize = 0;

    public GetFolderSize(final OnTaskCompleted listener, final Context context, final String email) {

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {

                return postData();
            }

            public String postData() {
                String line = "";
                BufferedReader in = null;

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://local-motion.ca/pass/server/getFolderSize_v1.php");

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
            protected void onPostExecute(String response) {
                Log.i(MyActivity.TAG, getClass().getName() + ": " + "Folder size: " + String.valueOf(response));

                try {
                    JSONObject content = new JSONObject(response);
                    folderSize = content.getInt("size");
                    listener.onTaskCompleted(folderSize, context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(email);
    }
}
