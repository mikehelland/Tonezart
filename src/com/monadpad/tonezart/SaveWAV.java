package com.monadpad.tonezart;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import com.monadpad.tonezart.dsp.UGen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * User: m
 * Date: 9/30/13
 * Time: 5:20 PM
 */
public class SaveWAV extends AsyncTask<Void, Void, Void> {

    private ProgressDialog pdialog = null;

    private Context mContext;


    private Uri lastSavedRingtone;

    public final static int SAVE_MODE_PLAIN = 0;
    public final static int SAVE_MODE_RINGTONE = 1;
    public final static int SAVE_MODE_RINGTONE_CONTACT = 2;
    public final static int SAVE_MODE_NOTIFICATION = 3;

    private boolean saved = false;

    private int duration;

    private boolean copyWhenFinished = false;
    private int copySaveMode;
    private String copyName;

    MonadaphoneThread monadThread;

    public SaveWAV(Context context, MonadaphoneThread thread) {
        super();

        mContext = context;
        monadThread = thread;

    }

    @Override
    protected Void doInBackground(Void... voids) {



        final long timer = System.currentTimeMillis();
        //android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);


        int samp = record();


        long l = System.currentTimeMillis() - timer;

        Log.d("MGH", Integer.toString(samp) + " samples in " +
                Long.toString(l) + " seconds. " + Float.toString((float)samp / (float)l) + " samples per second");

        return null;
    }

    protected void onPreExecute() {

        pdialog = ProgressDialog.show(mContext, "",
                "Saving Audio (WAV). Please wait...", true);
    }

    protected void onPostExecute(Void v) {

        Log.d("MGH", "saveWave.onpostexecute");

        if (pdialog != null){
            pdialog.dismiss();
        }

        saved = true;

        play();

        if (copyWhenFinished) {
            copy(copyName, copySaveMode);
        }
    }



    public void play(){
        Log.d("MGH", "copyAndPlay start");
        MediaPlayer mp = new MediaPlayer();

        try {
            mp.setDataSource(mContext.openFileInput("temp.wav").getFD());

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            mp.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mp.start();

        duration = mp.getDuration();
    }

    public void copy(String ojName, int saveMode) {
        if (ojName.length() == 0)
            return;

        File ringDir;
        File k = null;
        boolean saved = false;

        try{

            Log.d("MGH", "copyAndPlay 10");

            ringDir = RingtoneFileHelper.getRingtoneDirectory();
            if (!ringDir.exists())
                ringDir.mkdirs();
            FileChannel inChannel = mContext.openFileInput("temp.wav").getChannel();
            k = new File(ringDir, ojName + ".wav");
            FileChannel outChannel = new FileOutputStream(k).getChannel();

            inChannel.transferTo(0, inChannel.size(), outChannel);
            Toast.makeText(mContext, "File " + ojName + ".wav was saved.", Toast.LENGTH_LONG);

            inChannel.close();
            outChannel.close();
            saved = true;
        }
        catch (IOException e){
            Toast.makeText(mContext, "Problem. Do you have an SD Card? \n\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("MGH CopyAndPlay IOException", e.getMessage());
        }


        if (!saved ) {
            return;
        }

        Log.d("MGH", "copyAndPlay 20");

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, ojName);
        values.put(MediaStore.MediaColumns.SIZE, k.length());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav");
        values.put(MediaStore.Audio.Media.ARTIST, "Tonezart");
        values.put(MediaStore.Audio.Media.DURATION, duration);
        values.put(MediaStore.Audio.Media.IS_RINGTONE,
                saveMode == SAVE_MODE_RINGTONE || saveMode == SAVE_MODE_RINGTONE_CONTACT);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, saveMode == SAVE_MODE_NOTIFICATION);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);

        //Insert it into the database
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(k.getAbsolutePath());
        Uri newUri = mContext.getContentResolver().insert(uri, values);

        if (saveMode == SAVE_MODE_RINGTONE)
            RingtoneFileHelper.set(mContext, newUri);
        else if (saveMode == SAVE_MODE_NOTIFICATION)
            RingtoneFileHelper.setNotification(mContext, newUri);
        else if (saveMode == SAVE_MODE_RINGTONE_CONTACT) {

            lastSavedRingtone = newUri;
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            ((Activity)mContext).startActivityForResult(intent, ManualActvity.CONTACT_CHOOSER);

        }


        Log.d("MGH", "copyAndPlay 30");
    }


    public void copyWhenFinished(int saveMode, String name) {

        copySaveMode = saveMode;
        copyWhenFinished = true;
        copyName = name;
    }

    public void saveToContact(Intent data) {
        try {
            Uri contactData = data.getData();
            String cId = contactData.getLastPathSegment();

            String[] PROJECTION = new String[] {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
            };
            Cursor cur = mContext.getContentResolver().query(contactData, PROJECTION, null, null, null);
            cur.moveToFirst();

            String contactID = cur.getString(cur.getColumnIndexOrThrow("_id"));
            String contactDisplayName = cur.getString(cur.getColumnIndexOrThrow("display_name"));

            Uri localUrl = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactID);
            cur.close();
            ContentValues values = new ContentValues();

            values.put(ContactsContract.Data.RAW_CONTACT_ID, cId);
            values.put(ContactsContract.Data.CUSTOM_RINGTONE, lastSavedRingtone.toString());

            mContext.getContentResolver().update(localUrl, values, null, null);
            Toast.makeText(mContext, "Ringtone assigned to " + contactDisplayName, Toast.LENGTH_LONG).show();

        }
        catch (Exception e) {

            Log.d("MGH exception thingy", e.getMessage());
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();

        }

    }

    private int record() {

        PcmWriter pcm = new PcmWriter(mContext, "temp.pcm");

        MonadaphoneChannel mpc1;
        MonadaphoneChannel mpc2;

        mpc1 = monadThread.getChannel(0).clone();
        mpc2 = monadThread.getChannel(1).clone();

        mpc1.record(pcm);
        mpc2.record(pcm);

        int samples = 0;
        long now = 0;

        long timer = System.currentTimeMillis();

        float sampleRate = (float) UGen.SAMPLE_RATE;

        long stopAt = 0l;

        boolean done1, done2;

        while (stopAt == 0 || now < stopAt) {

            now = (long)((samples / sampleRate) * 1000);

            if (stopAt == 0) {
                done1 = !mpc1.update(now);
                done2 = !mpc2.update(now);

                if (done1 && done2)
                    stopAt = now + 3000;
            }

            mpc1.update();
            mpc2.update();

            samples+= UGen.CHUNK_SIZE;

        }

        Log.d("MGH timer", Long.toString(System.currentTimeMillis() - timer));

        pcm.finish();

        return samples;
    }


}




