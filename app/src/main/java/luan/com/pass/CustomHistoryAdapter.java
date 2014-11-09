package luan.com.pass;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Created by Luan on 2014-04-21.
 */
public class CustomHistoryAdapter extends BaseAdapter {
    ArrayList<HistoryItem> historyItems = new ArrayList<HistoryItem>();
    Context mContext = null;
    LayoutInflater inflater = null;

    public CustomHistoryAdapter(Context context, ArrayList<HistoryItem> items) {
        mContext = MyActivity.mContext;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        historyItems = items;
    }

    public CustomHistoryAdapter(Context context) {
        mContext = MyActivity.mContext;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return historyItems.size();
    }

    @Override
    public HistoryItem getItem(int arg0) {
        // TODO Auto-generated method stub
        return historyItems.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    public void addItem(ArrayList<HistoryItem> historyItems2) {
        historyItems.addAll(historyItems2);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View view, ViewGroup arg2) {
        view = inflater.inflate(R.layout.row_history, null);

        TextView messageText = (TextView) view.findViewById(R.id.message);
        ImageButton copyButton = (ImageButton) view.findViewById(R.id.copy);
        TextView dateTimeText = (TextView) view.findViewById(R.id.dateTime);
        String msg = HistoryFragment.historyItems.get(position).message;
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete);
        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send);
        ImageButton openButton = (ImageButton) view.findViewById(R.id.open);
        ImageButton shareButton = (ImageButton) view.findViewById(R.id.share);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.rowLayout);

/*        if(position%2==1){
            layout.setBackgroundColor(Color.parseColor("#F8F8F8"));
        }*/
        if (msg.equals("")) {
            messageText.setVisibility(View.GONE);
        } else {
            messageText.setText(HistoryFragment.historyItems.get(position).message);
        }
        dateTimeText.setText(HistoryFragment.historyItems.get(position).dateTime);
        if (HistoryFragment.historyItems.get(position).type.equals("file")) {
            messageText.setText(HistoryFragment.historyItems.get(position).message + "\nFile transfer: " +
                    HistoryFragment.historyItems.get(position).fileName);
            copyButton.setVisibility(View.GONE);
        } else if (HistoryFragment.historyItems.get(position).type.equals("image")) {
            if (HistoryFragment.historyItems.get(position).bitmap == null) {
                messageText.setText(HistoryFragment.historyItems.get(position).message + "\nImage transfer: " +
                        HistoryFragment.historyItems.get(position).fileName +
                        "\n Image not available on device. Tap to download.");
            }
            imageView.getLayoutParams().height = 300;
            copyButton.setVisibility(View.GONE);
        } else {
            openButton.setVisibility(View.GONE);
        }
        imageView.setImageBitmap(HistoryFragment.historyItems.get(position).bitmap);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyActivity.copyClipboard(HistoryFragment.historyItems.get(position).message);
                Toast.makeText(MyActivity.mContext, "Text copied", Toast.LENGTH_SHORT).show();
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                if (HistoryFragment.historyItems.get(position).type.equals("text")) {
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, HistoryFragment.historyItems.get(position).message);
                    shareIntent.setType("text/plain");
                } else {
                    String fileName = HistoryFragment.historyItems.get(position).fileName;
                    Uri hacked_uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
                    String mimeType = MyActivity.getMimeType(fileName);

                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, hacked_uri);
                    shareIntent.setType(mimeType);
                }

                MyActivity.mContext.startActivity(Intent.createChooser(shareIntent, "Pass"));
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(mContext, SendActivity.class);
                if (HistoryFragment.historyItems.get(position).type.equals("text")) {
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, HistoryFragment.historyItems.get(position).message);
                    sendIntent.setType("text/plain");
                } else {
                    String fileName = HistoryFragment.historyItems.get(position).fileName;
                    Uri hacked_uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
                    String mimeType = MyActivity.getMimeType(fileName);

                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, hacked_uri);
                    sendIntent.setType(mimeType);
                }

                MyActivity.mContext.startActivity(sendIntent);
            }
        });
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryFragment.openFile(position);
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryFragment.deleteHistory(HistoryFragment.historyItems.get(position).dbID, position);
            }
        });

        return (view);
    }

    @Override
    public int getViewTypeCount() {
        return (1);
    }

    public void updateEntries(ArrayList<HistoryItem> items) {
        historyItems = items;
        notifyDataSetChanged();
    }
}
