package com.monadpad.tonezart;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import com.monadpad.tonezart.dsp.UGen;


public class ManualActvity extends Activity {

	boolean firstTime = true;

    MonadaphoneView mpad;

    final static int CONTACT_CHOOSER = 73729;

    final static int DIALOG_SAVE = 3254;


    private AlertDialog dialog1;
    private AlertDialog dialog2;


    private PcmWriter pcmWriter = null;

    private SaveWAV saveWAV;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main_record);

        mpad = ((MonadaphoneView) findViewById(R.id.mpad));
        mpad.setActivity(this);

		findViewById(R.id.save_button).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                boolean save = !mpad.isFresh();
                mpad.resetClear();

                if (save) {
                    save();
                }

                onReset();
            }
        });

        findViewById(R.id.discardbutton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                mpad.hardReset();
                onReset();

            }
        });


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                ManualActvity.this, R.array.instruments, android.R.layout.simple_dropdown_item_1line);

        AlertDialog.Builder ab = new AlertDialog.Builder(ManualActvity.this);

        ab.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((MonadaphoneView) findViewById(R.id.mpad)).instrumentSettings(i);
            }
        });

        dialog1 = ab.create();

        ab = new AlertDialog.Builder(ManualActvity.this);

        ab.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((MonadaphoneView) findViewById(R.id.mpad)).instrumentSettings2(i);
            }
        });

        dialog2 = ab.create();


        findViewById(R.id.finger1button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.show();

            }
        });

        findViewById(R.id.finger2button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog2.show();

            }
        });


        findViewById(R.id.auto_mode_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ManualActvity.this, AutoActivity.class));
            }
        });
    }



    @Override
	public void onResume(){
		super.onResume();

		if (firstTime){
			firstTime = false;
		}
		else {
			((MonadaphoneView) findViewById(R.id.mpad)).reset();
		}

    }

    @Override
	public void onPause(){
		super.onPause();
		((MonadaphoneView) findViewById(R.id.mpad)).hardReset();
        //if (!isFinishing()){
        //    finish();
        //}
    }

    void onDrawingStarted() {

        findViewById(R.id.finger1button).setVisibility(View.GONE);
        findViewById(R.id.finger2button).setVisibility(View.GONE);
        findViewById(R.id.discardbutton).setVisibility(View.VISIBLE);
        findViewById(R.id.save_button).setVisibility(View.VISIBLE);

    }

    void onReset() {
        findViewById(R.id.finger1button).setVisibility(View.VISIBLE);
        findViewById(R.id.finger2button).setVisibility(View.VISIBLE);
        findViewById(R.id.discardbutton).setVisibility(View.GONE);
        findViewById(R.id.save_button).setVisibility(View.GONE);

    }


    @Override
    protected void onActivityResult(int req, int result, Intent data) {

        if (req == CONTACT_CHOOSER && result == RESULT_OK) {

            saveWAV.saveToContact(data);

        }

    }


    public void save(){

        showDialog(DIALOG_SAVE);

    }

    @Override
    protected Dialog onCreateDialog(int d) {

        if (d == DIALOG_SAVE) {
            final Dialog dl = new Dialog(this);
            dl.setTitle("Save your sound");
            dl.setContentView(R.layout.savewav);

            dl.findViewById(R.id.play_again_button).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    playAgain();

                }
            });

            dl.findViewById(R.id.save_ringtone_button).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    onChooseSaveMode(SaveWAV.SAVE_MODE_RINGTONE, dl);

                }
            });
            dl.findViewById(R.id.save_ringtone_contact_button).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    onChooseSaveMode(SaveWAV.SAVE_MODE_RINGTONE_CONTACT, dl);

                }
            });
            dl.findViewById(R.id.save_notification_button).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    onChooseSaveMode(SaveWAV.SAVE_MODE_NOTIFICATION, dl);

                }
            });

            Button okButton = (Button)dl.findViewById(R.id.save_plain_button);
            okButton.setOnClickListener( new OnClickListener() {
                public void onClick(View v) {
                    onChooseSaveMode(SaveWAV.SAVE_MODE_PLAIN, dl);

                }
            } );

            return dl;
        }
        return null;
    }

    private void onChooseSaveMode(int saveMode, Dialog dl) {

        MonadaphoneThread thread = mpad.getMonadaphoneThread();
        thread.reset(true);
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String name = ((EditText)dl.findViewById(R.id.txtOjName)).getText().toString();
        if (name.length() == 0)
            name = Long.toString(System.currentTimeMillis()) ;

        dl.hide();

        saveWAV = new SaveWAV(ManualActvity.this, thread);
        saveWAV.copyWhenFinished(saveMode, name);
        saveWAV.execute();

    }

    public void playAgain() {

        ReplayManualThread replay = new ReplayManualThread(mpad.getMonadaphoneThread());
        replay.start();

    }
}
