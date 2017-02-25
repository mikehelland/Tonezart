package com.monadpad.tonezart;

import android.util.Log;
import com.monadpad.tonezart.dsp.UGen;

/**
 * User: m
 * Date: 10/10/13
 * Time: 3:06 PM
 */
public class ReplayManualThread extends Thread {

    MonadaphoneThread monadThread;


    public ReplayManualThread(MonadaphoneThread thread) {

        monadThread = new MonadaphoneThread(thread);

    }

    @Override
    public void run() {

        MonadaphoneChannel mpc1;
        MonadaphoneChannel mpc2;

        mpc1 = monadThread.getChannel(0);
        mpc2 = monadThread.getChannel(1);

        long now = 0;

        long timer = System.currentTimeMillis();

        long stopAt = 0l;

        boolean done1, done2;

        while (stopAt == 0 || now < stopAt) {

            //now = (long)((samples / sampleRate) * 1000);
            now = System.currentTimeMillis() - timer;

            if (stopAt == 0) {
                done1 = !mpc1.update(now);
                done2 = !mpc2.update(now);

                if (done1 && done2)
                    stopAt = now + 3000;
            }

            mpc1.update();
            mpc2.update();


        }

        Log.d("MGH timer", Long.toString(System.currentTimeMillis() - timer));

    }


}
