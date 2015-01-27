package luan.com.flippit.utilities;

import android.content.Context;
import android.os.AsyncTask;

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
import luan.com.flippit.HistoryItem;

/**
 * Created by Luan on 2014-11-10.
 */
public class UpdateHistoryListview_v2 {
    static Callback callback;

    public UpdateHistoryListview_v2(final Callback callback) {
        this.callback = callback;
    }

    public static void updateListview(final int totalLoad, final String email, final String search, final Context mContext) {
/*        if (progressBar != null) {
            progressBar.setIndeterminate(true);
        }*/

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
                    nameValuePairs.add(new BasicNameValuePair("search", search));
                    nameValuePairs.add(new BasicNameValuePair("total", String.valueOf(totalLoad)));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpclient.execute(httppost);

                    in = new BufferedReader(new InputStreamReader(
                            response.getEntity().getContent()));

                    line = in.readLine();

                    historyItems = GeneralUtilities.historyJSONtoArray(line);

                    GeneralUtilities.saveHistoryResult2Prefs(mContext, line);

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block

                }
                return historyItems;
            }

            @Override
            protected void onPostExecute(ArrayList<HistoryItem> historyItems) {
                callback.callBackFinish(historyItems);
/*                if (progressBar != null) {
                    progressBar.setIndeterminate(false);
                }*/
            }
        }.execute();
    }

}
