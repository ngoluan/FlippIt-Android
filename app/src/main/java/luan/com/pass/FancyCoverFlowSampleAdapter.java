package luan.com.pass;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

class FancyCoverFlowSampleAdapter extends FancyCoverFlowAdapter {
    ArrayList<DeviceItem> deviceItems = new ArrayList<DeviceItem>();

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


        return reuseableView;
    }

    public void updateEntries(ArrayList<DeviceItem> items) {
        deviceItems = items;
        notifyDataSetChanged();
    }
}


    // =============================================================================
    // Private members
    // =============================================================================
    /*ArrayList<DeviceItem> deviceItems = new ArrayList<DeviceItem>();
    private Context mContext;
    private int[] images = {};

    // =============================================================================
    // Supertype overrides
    // =============================================================================
    public FancyCoverFlowSampleAdapter(Context c) {
        mContext = c;
    }

    @Override
    public int getCount() {
        return deviceItems.size();
    }

    @Override
    public Integer getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getCoverFlowItem(final int position, View reuseableView, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (reuseableView == null) {

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
            gridView = reuseableView;
        }


        return gridView;
    }

    public void updateEntries(ArrayList<DeviceItem> items) {
        deviceItems = items;
        notifyDataSetChanged();
    }*/
