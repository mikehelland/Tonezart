package com.monadpad.tonezart;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

/**
 * User: m
 * Date: 9/7/13
 * Time: 3:56 AM
 */
public class RingtoneFileHelper {

    public static boolean canUseStorage() {

        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static File getRingtoneDirectory() {

        File f;
        if (Build.VERSION.SDK_INT >= 8)
            f =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
        else {

            f = Environment.getExternalStorageDirectory();
            f = new File(f, "Ringtones/");
        }

        return f;

    }

    static void set(Context context, Uri uri) {
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, uri);
        Toast.makeText(context, "Ringtone Set", Toast.LENGTH_LONG).show();
    }

    static void setNotification(Context context, Uri uri) {
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, uri);
        Toast.makeText(context, "Notification Sound Set", Toast.LENGTH_LONG).show();
    }

}
