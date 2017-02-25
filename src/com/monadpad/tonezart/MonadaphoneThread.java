package com.monadpad.tonezart;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Process;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class MonadaphoneThread extends Thread {

    private ArrayList<MonadaphoneChannel> channels = new ArrayList<MonadaphoneChannel>();

    private MonadaphoneChannel liveChannel = null;

    private String scale;
    private int octaves;
    private int base;

    private long resetTime = 0;
    private int nextChannel = 0;

    public MonadaphoneThread(Context ctx, int instrument1, int instrument2){
        final SharedPreferences synthPrefs = PreferenceManager
        .getDefaultSharedPreferences(ctx);

        scale = synthPrefs.getString("quantizer", "0,2,4,5,7,9,11");
        octaves = Integer.parseInt(synthPrefs.getString("octaves", "4"));
        base = Integer.parseInt(synthPrefs.getString("base", "36"));

        newChannel(instrument1);

        newChannel(instrument2);

    }

    public MonadaphoneThread(MonadaphoneThread oldThread) {
        MonadaphoneChannel mpc;

        channels.add(oldThread.getChannel(0).clone());
        channels.add(oldThread.getChannel(1).clone());

    }

    public MonadaphoneChannel getChannel(int index) {
        return channels.get(index);
    }

    public MonadaphoneChannel getNextChannel(int instrument){
        MonadaphoneChannel mpc;
        if(nextChannel == 0){
            nextChannel = 1;
            mpc =  channels.get(0);
        }else{
            nextChannel = 0;
            mpc = channels.get(1);
        }
        mpc.unmute();
        return mpc;
    }

    public MonadaphoneChannel newChannel(int instrument) {
        MonadaphoneChannel mpC = new MonadaphoneChannel(instrument, scale, octaves, base);

        channels.add(mpC);

        return mpC;
    }



    @Override
    public void run() {
//        Log.d(TAG, "started audio rendering");

        System.gc();
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        //Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

        boolean keepRunning = true;
        while (keepRunning) {


            channels.get(0).update();
            channels.get(1).update();

            //pcmWriter.flush();

            // if its over, let's interuupt
            if (resetTime > 0 && System.currentTimeMillis() > resetTime){
                keepRunning = false;
            }

        }

    }

    public void reset(){
        reset(false);
    }
    
    public void reset(boolean hard) {

        channels.get(0).finish();
        channels.get(1).finish();


        resetTime = System.currentTimeMillis() + (hard ? 0 : 3300);
        //channels.clear();
    }


    public void drawChannels(Canvas canvas, Paint fPaint) {
        for (int ic = 0; ic < channels.size(); ic++) {
            channels.get(ic).draw(canvas);
        }
        if (!(liveChannel == null)){
            liveChannel.draw(canvas);
        }


    }

}

//            ugDac.close();
