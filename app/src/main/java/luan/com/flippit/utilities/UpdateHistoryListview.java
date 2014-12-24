package luan.com.flippit.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

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

import luan.com.flippit.GeneralUtilities;
import luan.com.flippit.HistoryFragment;
import luan.com.flippit.HistoryItem;
import luan.com.flippit.MyActivity;
import luan.com.flippit.widget.WidgetProvider;

/**
 * Created by Luan on 2014-11-10.
 */
public class UpdateHistoryListview {
    public UpdateHistoryListview(final int totalLoad, final String email, final ProgressBar progressBar, final RemoteViews view,
                                 final HistoryFragment.HistoryGetCallback historyGetCallback, final WidgetProvider.GetDataCallback getDataCallback) {
        if (progressBar != null) {
            progressBar.setIndeterminate(true);
        }

        new AsyncTask<String, Integer, ArrayList<HistoryItem>>() {
            @Override
            protected ArrayList<HistoryItem> doInBackground(String... params) {
                // TODO Auto-generated method stub

                return postData();
            }

            public ArrayList<HistoryItem> postData() {
                String line = "";
                ArrayList<HistoryItem> historyItems = new ArrayList<HistoryItem>();
                BufferedReader in = null;

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(GeneralUtilities.SERVER_PATH + "server/getHistory_v1.php");

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
                        historyItems.clear();
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject item = result.getJSONObject(i);
                            historyItems.add(new HistoryItem(
                                    item.getString("dateTime"),
                                    item.getString("message"),
                                    item.getString("targetID"),
                                    item.getString("fileName"),
                                    item.getInt("id")));
                            String type = GeneralUtilities.typeOfMessage(item.getString("fileName"));

                            if (type.equals("image")) {
                                Uri hacked_uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOWNLOADS) + "/" + item.getString("fileName"));
                                String path = Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOWNLOADS) + "/" + item.getString("fileName");
                                File file = new File(path);
                                if (file.exists()) {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                                    historyItems.get(historyItems.size() - 1).bitmap = DecodeSampledBitmapFromPath.decodeSampledBitmapFromPath(path, 1000, 1000, options);
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
                return historyItems;
            }

            @Override
            protected void onPostExecute(ArrayList<HistoryItem> historyItems) {
                Log.i(MyActivity.TAG, getClass().getName() + ": " + "Callback: " + String.valueOf(getDataCallback));
                if (historyGetCallback != null) {
                    historyGetCallback.callBack(historyItems);
                }
                if (getDataCallback != null) {
                    getDataCallback.callBackFinish(historyItems);
                }
                if (progressBar != null) {
                    progressBar.setIndeterminate(false);
                }

            }
        }.execute();
    }
    /**
     * Created by Luan on 2014-11-11.
     */

}
