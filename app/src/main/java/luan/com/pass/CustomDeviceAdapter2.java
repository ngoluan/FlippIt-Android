package luan.com.pass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Luan on 2014-10-28.
 */
public class CustomDeviceAdapter2 extends BaseAdapter {
    ArrayList<DeviceItem> deviceItems = new ArrayList<DeviceItem>();
    private Context mContext;

    public CustomDeviceAdapter2(Context c) {
        mContext = c;
    }

    public int getCount() {
        return deviceItems.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(mContext);

            // get layout from mobile.xml
            gridView = inflater.inflate(R.layout.item_device, null);

            // set value into textview
            TextView textView = (TextView) gridView
                    .findViewById(R.id.textView);
            textView.setText(deviceItems.get(position).name);
            if (deviceItems.get(position).name.equals("")) {
                textView.setText(deviceItems.get(position).type);
            }
            ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.imageView);
            if (deviceItems.get(position).type.equals("chrome")) {
                imageView.setImageResource(R.drawable.computer_black);
            } else if (deviceItems.get(position).type.equals("android")) {
                imageView.setImageResource(R.drawable.phone_black);
            } else {
                imageView.setImageResource(R.drawable.cloud_black);
            }
        } else {
            gridView = (View) convertView;
        }
        RelativeLayout layout = (RelativeLayout) gridView.findViewById(R.id.layout);
        if (deviceItems.get(position).targetID.equals(SendActivity.targetID)) {
            layout.setBackground(SendActivity.mContext.getResources().getDrawable(R.drawable.rounded_yellow));
        } else {
            layout.setBackground(SendActivity.mContext.getResources().getDrawable(R.drawable.rounded_blue));
        }
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(MyActivity.TAG, String.valueOf(position));
                String targetID = deviceItems.get(position).targetID;
                SendActivity.targetID = targetID;
                String targetType = deviceItems.get(position).type;
                SharedPreferences.Editor editor = SendActivity.mPrefs.edit();
                editor.putString("targetID", targetID);
                editor.putString("targetType", targetType);
                editor.commit();
                updateEntries(deviceItems);
            }
        });
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setTitle("Delete device?");

// set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Toast.makeText()
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

// create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

// show it
                alertDialog.show();
                return true;
            }
        });
        return gridView;
    }


    public void updateEntries(ArrayList<DeviceItem> items) {
        deviceItems = items;
        notifyDataSetChanged();
    }
}
