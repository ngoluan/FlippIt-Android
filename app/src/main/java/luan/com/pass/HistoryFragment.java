package luan.com.pass;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import luan.com.pass.utilities.GetFolderSize;
import luan.com.pass.utilities.HistoryGetCallbackInterface;
import luan.com.pass.utilities.OnTaskCompleted;
import luan.com.pass.utilities.UpdateHistoryListview;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {
    static public CustomHistoryAdapter customHistoryAdapter = null;
    static public Boolean flagLoading = false;
    static public int totalLoad = 20;
    static public int lastHistoryTotal = 99;
    static ArrayList<HistoryItem> historyItems = new ArrayList<HistoryItem>();
    static ListView historyList = null;
    static ProgressBar progressBar = null;
    View mView = null;

    public HistoryFragment() {
        // Required empty public constructor
    }

    static public void createListView(final int totalLoad) {
        String email = MyActivity.mPrefs.getString("email", "");
        HistoryGetCallback historyCallBack = new HistoryGetCallback();
        new UpdateHistoryListview(totalLoad, email, progressBar, null, historyCallBack, null);
    }


    static public void deleteHistoryAll() {
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
                HttpPost httppost = new HttpPost("http://local-motion.ca/pass/deleteMessage.php");
                String email = MyActivity.mPrefs.getString("email", "");
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("id", "all"));
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
                historyItems.clear();
                animateDelete();
            }
        }.execute();
    }

    static public void animateDelete() {
        final Animation animation = AnimationUtils.loadAnimation(MyActivity.mContext,
                R.anim.abc_slide_out_top);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                customHistoryAdapter.updateEntries(historyItems);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        historyList.startAnimation(animation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_history, container,
                false);
        progressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
        historyList = (ListView) mView.findViewById(R.id.listView);

        customHistoryAdapter = new CustomHistoryAdapter(MyActivity.mContext);
        historyList.setAdapter(customHistoryAdapter);
        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view,
                                    int position, long id) {

            }
        });
        createListView(totalLoad);
        historyList.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 1) {
                    if (flagLoading == false) {
                        if (lastHistoryTotal == historyItems.size()) {
                            return;
                        }
                        lastHistoryTotal = historyItems.size();
                        flagLoading = true;
                        totalLoad += 20;
                        createListView(totalLoad);
                    }
                }
            }
        });
        OnTaskCompleted Callback = new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(int folderSize, Context context) {
                Log.i(GeneralUtilities.TAG, MyActivity.mContext.getClass().getName() + ": " + "Folder size: " + String.valueOf(folderSize));
                if (folderSize > GeneralUtilities.FOLDER_LIMIT) {
                    int limit = (int) ((float) (folderSize / (GeneralUtilities.FOLDER_LIMIT)) * 100);
                    Toast.makeText(context, "You have reached " + String.valueOf(limit) + "% of your space. Consider deleting some messages.", Toast.LENGTH_LONG).show();
                }
            }
        };
        String email = MyActivity.mPrefs.getString("email", "");
        new GetFolderSize(Callback, MyActivity.mContext, email);

        ImageButton sendButton = (ImageButton) mView.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(MyActivity.mContext, SendActivity.class);
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyActivity.mContext.startActivity(sendIntent);
            }
        });

        return mView;
    }

    public void animateUpdateListview() {

    }

    public static class HistoryGetCallback implements HistoryGetCallbackInterface {
        @Override
        public void callBack(ArrayList<HistoryItem> historyItems) {
            HistoryFragment.flagLoading = false;
            HistoryFragment.historyItems = historyItems;
            HistoryFragment.customHistoryAdapter.updateEntries(historyItems);
        }

        @Override
        public void callBack(int i) {

        }
    }

    public static class DeleteHistoryCallback implements HistoryGetCallbackInterface {
        @Override
        public void callBack(ArrayList<HistoryItem> historyItems) {

        }

        @Override
        public void callBack(int i) {
            HistoryFragment.historyItems.remove(i);
            HistoryFragment.animateDelete();
        }
    }
}
