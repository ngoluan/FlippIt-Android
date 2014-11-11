import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;

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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import luan.com.pass.CustomHistoryAdapter;
import luan.com.pass.HistoryItem;
import luan.com.pass.MyActivity;

/**
 * Created by Luan on 2014-11-10.
 */
public class UpdateHistoryListview
{
    public  UpdateHistoryListview(int totalLoad, ArrayList<HistoryItem> historyItems, ProgressBar progressBar, SharedPreferences mPrefs,
                                  CustomHistoryAdapter adapter, CustomWidgetService customWidgetService){
        final String email = mPrefs.getString("email", "");
        progressBar.setIndeterminate(true);
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                // TODO Auto-generated method stub
                String result = postData();
                return "";
            }

            public String postData() {
                String line = "";
                BufferedReader in = null;

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://local-motion.ca/pass/getHistory.php");

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                    nameValuePairs.add(new BasicNameValuePair("email", email));

                    nameValuePairs.add(new BasicNameValuePair("total", String.valueOf(totalLoad)));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpclient.execute(httppost);

                    in = new BufferedReader(new InputStreamReader(
                            response.getEntity().getContent()));

                    line = in.readLine();

                    try {
                        JSONArray result = new JSONArray(line);
                        UpdateHistoryListview.this.hi
                        historyItems.clear();
                        Log.i(MyActivity.TAG, getClass().getName() + ": " + "Received history. Total messages: " + result.length());
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject item = result.getJSONObject(i);
                            historyItems.add(new HistoryItem(
                                    item.getString("dateTime"),
                                    item.getString("message"),
                                    item.getString("targetID"),
                                    item.getString("fileName"),
                                    item.getInt("id")));
                            String type = MyActivity.typeOfMessage(item.getString("fileName"));

                            if (type.equals("image")) {
                                Uri hacked_uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOWNLOADS) + "/" + item.getString("fileName"));
                                String path = Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOWNLOADS) + "/" + item.getString("fileName");
                                File file = new File(path);
                                if (file.exists()) {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                                    historyItems.get(historyItems.size() - 1).bitmap = decodeSampledBitmapFromPath(path, 1000, 1000, options);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block

                }
                return "";
            }
            @Override
            protected void onPostExecute(String msg) {
                flagLoading =false;
                customHistoryAdapter.updateEntries(historyItems);
                progressBar.setIndeterminate(false);

            }
        }.execute();
    }
}
