package luan.com.flippit;

import android.graphics.Bitmap;

import java.io.UnsupportedEncodingException;

public class HistoryItem {

    public String dateTime;
    public String message;
    public String fileName;
    public String targetID;
    public Bitmap bitmap;
    public int dbID;
    public String type;

    public HistoryItem(String dateTime, String message, String targetID, String fileName, int id) {
        this.dateTime = dateTime;
        this.bitmap = null;
        try {
            this.message = java.net.URLDecoder.decode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.targetID = targetID;
        this.fileName = fileName;
        this.dbID = id;
        this.type = GeneralUtilities.typeOfMessage(this.fileName);
    }

}
