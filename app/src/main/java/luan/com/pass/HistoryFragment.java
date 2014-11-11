package luan.com.pass;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import luan.com.pass

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {
    static ArrayList<HistoryItem> historyItems = new ArrayList<HistoryItem>();
    static CustomHistoryAdapter customHistoryAdapter = null;
    View mView = null;
    static ListView historyList = null;
    static Boolean flagLoading = false;
    static public int totalLoad = 20;
    static public int lastHistoryTotal = 99;
    static ProgressBar progressBar = null;

    public HistoryFragment() {
        // Required empty public constructor
    }

    static public void createListView(final int totalLoad) {
        HistoryCallBack historyCallBack = new HistoryCallBack();
        UpdateHistoryListview updateHistoryListview = new UpdateHistoryListview();

    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromPath(String path,
                                                         int reqWidth, int reqHeight, BitmapFactory.Options options) {

        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
    static public void deleteHistory(final int id, final int position) {
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
                    nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
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
                historyItems.remove(position);
                animateDelete();
            }
        }.execute();
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
    static public void animateDelete(){
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
    static public void openFile(int position) {
        String fileName = HistoryFragment.historyItems.get(position).fileName;
        Uri hacked_uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
        String mimeType = MyActivity.getMimeType(fileName);

        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
        String email = MyActivity.mPrefs.getString("email", "");
        DownloadFiles downloadFiles = new DownloadFiles();
        NotificationManager mNotificationManager = (NotificationManager)
                MyActivity.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MyActivity.mContext);
        if (!file.exists()) {
            if (HistoryFragment.historyItems.get(position).type.equals("file")) {
                Log.i(MyActivity.TAG, "Downloading file.");
                MyActivity.Callback sendFileNotificationInterface = new GcmIntentService.SendFileNotificationInterface();
                MyActivity.Callback sendFileUpdateNotificationInterface = new GcmIntentService.SendFileUpdateNotificationInterface();
                downloadFiles.getFileFromServer(email, HistoryFragment.historyItems.get(position).fileName, null,
                        HistoryFragment.historyItems.get(position).message, sendFileNotificationInterface, sendFileUpdateNotificationInterface,
                        MyActivity.mContext, mNotificationManager, mBuilder);
                return;
            } else if (HistoryFragment.historyItems.get(position).type.equals("image")) {
                Log.i(MyActivity.TAG, "Downloading image.");
                MyActivity.Callback sendImageNotificationInterface = new SendImageNotificationInterface();
                MyActivity.Callback sendImageUpdateNotificationInterface = new GcmIntentService.SendImageUpdateNotificationInterface();
                downloadFiles.getImageFromServer(email, HistoryFragment.historyItems.get(position).fileName, null,
                        HistoryFragment.historyItems.get(position).message, sendImageNotificationInterface, sendImageUpdateNotificationInterface,
                        MyActivity.mContext, mNotificationManager, mBuilder);
                return;
            }
        }
        //Log.d(MyActivity.TAG, String.valueOf(position));
        Intent intentOpen = new Intent();
        intentOpen.setAction(Intent.ACTION_VIEW);
        intentOpen.setDataAndType(hacked_uri, mimeType);
        MyActivity.mContext.startActivity(Intent.createChooser(intentOpen, "Pass"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_history, container,
                false);
        progressBar = (ProgressBar)mView.findViewById(R.id.progressBar);
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

                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount>1)
                {
                    if(flagLoading == false)
                    {
                        if(lastHistoryTotal==historyItems.size()){
                            return;
                        }
                        lastHistoryTotal=historyItems.size();
                        flagLoading = true;
                        totalLoad+=20;
                        createListView(totalLoad);
                    }
                }
            }
        });

        // Inflate the layout for this fragment
        return mView;
    }
    public void animateUpdateListview(){

    }
/*    class OpenFileInterface implements MyActivity.Callback {
        @Override
        public void callBack(String position, Bitmap image) {
            openFile(Integer.parseInt(position));
        }

        @Override
        public void callBack(String fileName, String msg, Bitmap image) {

        }
    }*/
    static class HistoryCallBack implements HistoryCallBackInterface {
        @Override
        public void callBack(int totalLoad) {

        }

    }
}
