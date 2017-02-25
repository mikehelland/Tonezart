package com.monadpad.tonezart;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * User: m
 * Date: 10/8/13
 * Time: 10:17 PM
 */
public class HeadBob {

    private View mView;

    private AnimateThread mThread;
    private Activity mActivity;
    private int beatMS;

    private int center;

    public HeadBob(Activity activity) {

        mActivity = activity;

        mView = activity.findViewById(R.id.leibniz_head);

        final View playSomethingElse = activity.findViewById(R.id.something_else_button);
        final View save = activity.findViewById(R.id.save_this_button);

        mView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                center = playSomethingElse.getTop() - (save.getHeight() + save.getTop());
                center = center / 2;
                center -= mView.getHeight() / 2;

                mView.getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });


    }

    public void start(int beatMS) {
        this.beatMS = beatMS;
        mThread = new AnimateThread();
        mThread.start();

    }


    private class AnimateThread extends Thread {

        boolean cancel = false;
        long started;

        @Override
        public void run() {

            started = System.currentTimeMillis();

            final int height = 10;
            final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)mView.getLayoutParams();

            Runnable setParamsRunnable = new Runnable() {
                @Override
                public void run() {
                    mView.setLayoutParams(params);
                }
            };

            float part1;
            float part2;
            float part3;

            while (!cancel) {

                part1 = (System.currentTimeMillis() - started) % beatMS;
                part2 = (part1) / beatMS;

                if (part2 < 0.2f) {
                    part3 = height;
                }
                else if (part2 < 0.7f) {
                    part3 = height - height * ((part2 - 0.2f) / 0.5f);
                }
                else {
                    part3 = height * ((part2 - 0.7f)/ 0.3f);
                }

                params.topMargin = center + (int)part3;

                mActivity.runOnUiThread(setParamsRunnable);

                try {
                Thread.sleep(1000 / 60);
                } catch (InterruptedException e) {
                    cancel = true;
                }
            }

        }

    }

    public void restart() {
        if (mThread.cancel) {
            mThread = new AnimateThread();
            mThread.start();
        }
        else {
            mThread.started = System.currentTimeMillis();
        }
    }

    public void finish() {
        mThread.cancel = true;
    }

}
