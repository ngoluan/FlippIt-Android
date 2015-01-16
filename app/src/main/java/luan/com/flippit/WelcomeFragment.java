package luan.com.flippit;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import luan.com.flippit.utilities.Callback;
import luan.com.flippit.utilities.FileCallback;


public class WelcomeFragment extends Fragment {
    View mView = null;
    Context mContext = null;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_welcome, container,
                false);
        mContext = container.getContext();
        Button startedBtn = (Button) mView.findViewById(R.id.getStarted);
        Button contBtn = (Button) mView.findViewById(R.id.welcomeContinue);

        startedBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String fileName = "getstarted.pptx";
                Uri hacked_uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
                String mimeType = GeneralUtilities.getMimeType(fileName);
                SharedPreferences mPrefs = mContext.getSharedPreferences(mContext.getPackageName(),
                        Context.MODE_PRIVATE);
                String email = mPrefs.getString("email", "");
                NotificationManager mNotificationManager = (NotificationManager)
                        mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);

                Log.i(MyActivity.TAG, getClass().getName() + ": " + "File transfer.");

                mBuilder.setContentTitle("FlippIt")
                        .setContentText("Downloading...")
                        .setSmallIcon(R.drawable.action_icon);

                String url = GeneralUtilities.SERVER_PATH + "server/" + fileName;

                Bundle extras = new Bundle();
                extras.putString("email", email);
                extras.putString("filename", fileName);
                extras.putString("msgId", "");


                DownloadFiles downloadFiles = new DownloadFiles(mContext);
                Callback fileCallback = new FileCallback(mContext);
                downloadFiles.getFileFromServer_v2(url, extras, fileCallback);

                Intent intentOpen = new Intent();
                intentOpen.setAction(Intent.ACTION_VIEW);
                intentOpen.setDataAndType(hacked_uri, mimeType);

                intentOpen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Intent new_intent = Intent.createChooser(intentOpen, "FlippIt");
                new_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.getApplicationContext().startActivity(new_intent);
            }
        });
        contBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                MyActivity.mActionBar.show();
                MyActivity.mFragmentManager.beginTransaction()
                        .replace(R.id.container, new HistoryFragment())
                        .commit();
            }
        });

        // Inflate the layout for this fragment
        return mView;
    }


}
