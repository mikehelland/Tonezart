package com.monadpad.tonezart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

class MonadaphoneView extends View {

    private MonadaphoneThread mThread ;
    private MonadaphoneChannel currentChannel;
    private MonadaphoneChannel secondChannel;

    private int firstId = -1;
    private int secondId = -1;


	Paint pathPaint = new Paint();
    final Paint fingerPaint = new Paint();

	float x, y;
	boolean isTouching = false;
	float lastX;
	float lastY;
	float lastx;
	long lastUp = 0;

	int instrument = 0;
    int instrument2 = 0;

	int tutorial = 0;
    final private Context ctx;

    private long startedAt;

    public MonadaphoneView(Context context, AttributeSet attrs) {
		super(context, attrs);
        ctx = context;

		setBackgroundColor(0xFF000000);

		pathPaint.setARGB(255, 255, 255, 255);
		pathPaint.setStyle(Paint.Style.STROKE);
		pathPaint.setStrokeWidth(4);
		pathPaint.setShadowLayer(6, 0, 0, 0xFFFFFFFF);

        fingerPaint.setARGB(128, 255, 255, 255);
		fingerPaint.setStyle(Paint.Style.FILL);


		setKeepScreenOn(true);
		setFocusable(true);

      //  mThread = new MonadaphoneThread(this, pcmWriter);
	}
    private Activity mActivity;
    public void setActivity(ManualActvity a){
        mActivity = a;
    }

	public void resetClear(){
        reset();
	}

    public void reset(){
        if (!fresh){
            mThread.reset();
            /*try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
        }

        lastUp = 0;
        fresh = true;

        invalidate();
	}
    public void hardReset(){

        if (!fresh){
            mThread.reset(true);
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        lastUp = 0;
        fresh = true;

        invalidate();

    }



	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();

		x = ex / getWidth();
		y = 1 - ey / getHeight();

        int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN : {

                ((ManualActvity)mActivity).onDrawingStarted();

                Log.d("MGH pointer count", Integer.toString(event.getPointerCount()));

                firstId = event.getPointerId(0);

                isTouching = true;
                if (fresh) {

                    startedAt = System.currentTimeMillis();

                    mThread = new MonadaphoneThread(ctx, instrument, instrument2);
                    mThread.start();

                    fresh = false;
                }

                instrumentSettings(instrument);

                currentChannel = mThread.getNextChannel(instrument);

                currentChannel.addXY(System.currentTimeMillis() - startedAt, ex, ey, x, y);

                break;
            }


            case  MotionEvent.ACTION_UP : {

                firstId = -1;

                Log.d("MGH pointer UP ", Integer.toString(event.getPointerCount()));

                if (isTouching)
                {
                    currentChannel.addXY(System.currentTimeMillis() - startedAt, -1, -1, -1, -1);

                    isTouching = false;

                    currentChannel.mute();
                    currentChannel = null;

                }
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN : {
                final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                Log.d("MGH pointer count 2", Integer.toString(event.getPointerCount()));

                if (secondChannel != null) {
                    break;
                }

                if (secondId > -1) {
                    break;
                }

                secondId = event.getPointerId(index);

                secondChannel = mThread.getNextChannel(instrument);

                break;
            }

            case MotionEvent.ACTION_POINTER_UP : {

                final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int id = event.getPointerId(index);

                Log.d("MGH UP 2", Integer.toString(event.getPointerCount()));

                long time = System.currentTimeMillis() - startedAt;
                if (id == firstId) {
                    currentChannel.addXY(time, -1, -1, -1, -1);
                    currentChannel.mute();
                    firstId = secondId;
                    secondId = -1;
                    currentChannel = secondChannel;
                    secondChannel = null;

                }
                else if (id == secondId) {
                    secondChannel.addXY(time, -1, -1, -1, -1);
                    secondChannel.mute();
                    secondId = -1;
                    secondChannel = null;
                }

                break;
            }

            case MotionEvent.ACTION_MOVE : {

                if (!isTouching)
                    break;

                int id;
                for (int ip = 0; ip  < event.getPointerCount(); ip++) {
                    id = event.getPointerId(ip);

                    ex = event.getX(ip);
                    ey = event.getY(ip);

                    x = ex / getWidth();
                    y = 1 - ey / getHeight();

                    long time = System.currentTimeMillis() - startedAt;
                    if (id == firstId) {
                        currentChannel.addXY(time, ex, ey, x, y);
                    }

                    if (id == secondId) {
                        secondChannel.addXY(time, ex, ey, x, y);
                    }

                }
                break;
            }
        }

        lastx = x;
        lastX = ex;
        lastY = ey;

        invalidate();

        return true;


    }




    private boolean fresh = true;

    private boolean hasDrawn = false;

	@Override
	public void onDraw(Canvas canvas) {

        //canvas.drawLine(0, 1, getWidth(),  1, fingerPaint);


        if (!hasDrawn) {
            fingerPaint.setTextSize(getWidth() < getHeight() || getWidth() > 600 ? 26 : 16);
            hasDrawn = true;
        }

        float center = getHeight() / 2.0f;

        String cap;
        float twidth;
		if (fresh && tutorial == 0) {

            cap = getResources().getString(R.string.record_caption_1);
            twidth = fingerPaint.measureText(cap);

            canvas.drawText(cap,
                    getWidth() / 2.0f - twidth / 2.0f, center - fingerPaint.getTextSize() * 1.5f, fingerPaint);

            cap = getResources().getString(R.string.record_caption_2);
            twidth = fingerPaint.measureText(cap);

            canvas.drawText(cap,
                    getWidth() / 2.0f - twidth / 2.0f, center + fingerPaint.getTextSize() * 1.5f, fingerPaint);


        }
        else{
            cap = getResources().getString(R.string.record_caption_3);
            twidth = fingerPaint.measureText(cap);

            canvas.drawText(cap,
                    getWidth() / 2.0f - twidth / 2.0f, center - fingerPaint.getTextSize() * 1.5f, fingerPaint);

            cap = getResources().getString(R.string.record_caption_4);
            twidth = fingerPaint.measureText(cap);

            canvas.drawText(cap,
                    getWidth() / 2.0f - twidth / 2.0f, center + fingerPaint.getTextSize() * 1.5f, fingerPaint);


            mThread.drawChannels(canvas, fingerPaint);
            if (isTouching) {
                canvas.drawCircle(lastX, lastY, 15, fingerPaint);
            }
        }
	}




	public void instrumentSettings(int pos){
		instrument = pos;
	}


    public void instrumentSettings2(int pos){
        instrument2 = pos;
    }

    public MonadaphoneThread getMonadaphoneThread() {
        return mThread;
    }

    public boolean isFresh() {
        return fresh;
    }
}
