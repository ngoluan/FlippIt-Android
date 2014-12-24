package luan.com.flippit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;

class CustomDeviceAdapter extends at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter {
    static ArrayList<DeviceItem> deviceItems = new ArrayList<DeviceItem>();
    static Context mContext = null;

    public CustomDeviceAdapter(Context context) {
        mContext = context;
    }

    static public void updateEntries(ArrayList<DeviceItem> items) {
        deviceItems = items;
        SendActivity.customDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return deviceItems.size();
    }

    @Override
    public DeviceItem getItem(int i) {
        return deviceItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getCoverFlowItem(final int position, View reuseableView, final ViewGroup viewGroup) {

        if (reuseableView == null) {
            reuseableView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_device, viewGroup, false);
            reuseableView.setLayoutParams(new FancyCoverFlow.LayoutParams(400, 400));


            // set value into textview
            TextView textView = (TextView) reuseableView
                    .findViewById(R.id.textView);
            textView.setText(deviceItems.get(position).name);
            if (deviceItems.get(position).name.equals("")) {
                textView.setText(deviceItems.get(position).type);
            }
            ImageView imageView = (ImageView) reuseableView
                    .findViewById(R.id.imageView);
            if (deviceItems.get(position).type.equals("chrome")) {
                imageView.setImageResource(R.drawable.computer_black);
            } else if (deviceItems.get(position).type.equals("android")) {
                imageView.setImageResource(R.drawable.phone_black);
            } else {
                imageView.setImageResource(R.drawable.cloud_black);
            }
        }
        RelativeLayout layout = (RelativeLayout) reuseableView.findViewById(R.id.layout);
        if (deviceItems.get(position).targetID.equals(SendActivity.targetID)) {
            layout.setBackground(SendActivity.mContext.getResources().getDrawable(R.drawable.rounded_yellow));
        } else {
            layout.setBackground(SendActivity.mContext.getResources().getDrawable(R.drawable.rounded_blue));
        }


        layout.setTag(position);


        /*layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(viewGroup.getContext());
                String[] choices = {"Change device name", "Delete device"};
                alertDialogBuilder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            final Dialog dialog = new Dialog(viewGroup.getContext());
                            dialog.setTitle("Change device name to:");
                            dialog.setContentView(R.layout.dialog_devicename);
                            dialog.show();

                            Button saveButton = (Button) dialog.findViewById(R.id.saveButtonDialog);
                            saveButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            });
                        } else {
                            AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(viewGroup.getContext());

                            deleteDialogBuilder.setPositiveButton("Delete device?", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            deleteDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            AlertDialog deleteDialog = deleteDialogBuilder.create();
                            deleteDialog.show();
                        }
                    }
                });
                AlertDialog dialog = alertDialogBuilder.create();
                dialog.show();
                return false;
            }
        });*/

        return reuseableView;
    }
}

