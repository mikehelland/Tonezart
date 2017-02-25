package com.monadpad.tonezart;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.monadpad.tonezart.dsp.UGen;

/**
 * User: m
 * Date: 9/30/13
 * Time: 2:49 AM
 */
public class TonezartJam {

    private MelodyMaker mMelodyMaker;

    private NotePlayer mNotePlayerLeft;
    private NotePlayer mNotePlayerRight;

    private PlayThread mThread;
    private MonadaphoneThread mMonadThread;

    private long duration = 2000;

    private int loops = 0;

    private int beatMs = 500;

    private PcmWriter pcm;

    private Context mContext;

//    private SaveWAV saveWAV;

    private JamListener mJamListener;

    private NoteLine mMelody1;
    private NoteLine mMelody2;

    public TonezartJam(Context context) {

        mContext = context;
        pcm = new PcmWriter(context, "temp.pcm");

        // make a melody
        mMelodyMaker = new MelodyMaker(context);
        mMelody1 = mMelodyMaker.makeMelody(4.0f);

        //Toast.makeText(mContext, mMelodyMaker.getKeyName(), Toast.LENGTH_LONG).show();

        mMelody1 = mMelodyMaker.applyScale(mMelody1);

        // get it ready for beats yo
        mMelody1.prepareNotes(beatMs);

        mMelody2 = mMelodyMaker.makeMelody(4.0f);
        mMelody2 = mMelodyMaker.applyScale(mMelody2);
        mMelody2.prepareNotes(beatMs);


        play();
    }

    public void play() {

        loops = 0;

        mMonadThread = new MonadaphoneThread(mContext,
                mMelody1.getInstrument(), mMelody2.getInstrument());

        MonadaphoneChannel mpc;
        mpc = mMonadThread.getChannel(0);

        mNotePlayerLeft = new NotePlayer(mpc);

        mpc = mMonadThread.getChannel(1);

        mNotePlayerRight = new NotePlayer(mpc);

//        pcm.start();
        mMonadThread.start();

        mNotePlayerLeft.setLine(mMelody1);
        mNotePlayerRight.setLine(mMelody2);
        mNotePlayerRight.mute();

        mThread = new PlayThread();
        mThread.start();


    }

    public void finish() {
        mThread.cancel = true;

    }


    class PlayThread extends Thread {

        boolean cancel = false;

        @Override
        public void run() {
            long nowInLoop;

            long loopStarted = System.currentTimeMillis();
            long currentLoopStarted = loopStarted;
            long now;

            while (!cancel) {

                now = System.currentTimeMillis();
                nowInLoop = now - currentLoopStarted;

                if (nowInLoop >= duration) {
                    currentLoopStarted += duration;

                    mNotePlayerLeft.resetI();
                    mNotePlayerRight.resetI();

                    if (!onNewLoop()) {
                        cancel = true;

                        if (mJamListener != null) {
                            mJamListener.onFinish();
                        }

                        break;
                    }
                }

                nowInLoop = now - loopStarted;

                mNotePlayerLeft.update(nowInLoop);
                mNotePlayerRight.update(nowInLoop);

            }

            mMonadThread.reset();


        }

    }

    private boolean onNewLoop() {
        loops++;

        if (loops == 4) {
            //mNotePlayerRight.setLine(mMelodyMaker.applyScale(mMelodyMaker.getMelody()).prepareNotes(beatMs));
            mNotePlayerRight.unmute();
        } else if (loops == 8) {

            return false;
        }

        return true;
    }


    public void record() {

        MonadaphoneChannel mpc1;
        MonadaphoneChannel mpc2;
        mpc1 = new MonadaphoneChannel(mMelody1.getInstrument(), "", 0, 0);
        mpc2 = new MonadaphoneChannel(mMelody2.getInstrument(), "", 0, 0);

        mpc1.record(pcm);
        mpc2.record(pcm);

        mNotePlayerLeft.mpc = mpc1;
        mNotePlayerRight.mpc = mpc2;

        mNotePlayerLeft.resetI();
        mNotePlayerRight.resetI();

        mNotePlayerRight.mute();


        loops = 0;

        int samples = 0;
        long now;

        int loop = 1;

        long timer = System.currentTimeMillis();

        float sampleRate = (float) UGen.SAMPLE_RATE;

        long stopAt = 0;

        boolean looping = true;
        while (true) {

            now = (long)((samples / sampleRate) * 1000);

            if (!looping) {
                if (now > stopAt) {
                    break;
                }
            }
            if (looping) {

                if (now >= loop * duration) {
                    loop++;

                    mNotePlayerLeft.resetI();
                    mNotePlayerRight.resetI();

                    if (!onNewLoop()) {
                        looping = false;

                        stopAt = now + 3000;

                        mpc1.mute();
                        mpc2.mute();
                        continue;

                    }
                }

                mNotePlayerLeft.update(now);
                mNotePlayerRight.update(now);
            }

            mpc1.update();
            mpc2.update();

            //pcm.flush();

            samples+= UGen.CHUNK_SIZE;

        }

        Log.d("MGH timer", Long.toString(System.currentTimeMillis() - timer));

        pcm.finish();


        mNotePlayerLeft.resetI();
        mNotePlayerRight.resetI();

        loops = 0;

    }

    public int getBeatMs() {
        return beatMs;
    }


    public void setJamListener(JamListener listener) {

        mJamListener = listener;
    }

    public void finishHard() {
        mThread.cancel = true;
        mMonadThread.reset(true);
    }
}
