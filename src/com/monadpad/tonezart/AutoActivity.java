package com.monadpad.tonezart;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class AutoActivity extends Activity {

    final static int DIALOG_SAVE = 3254;
    final static int CONTACT_CHOOSER = 73729;

    private HeadBob headBob;

    private TonezartJam mJam;

    private SaveAudio saveAudio;

    private boolean backFromContacts = false;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.auto);

        findViewById(R.id.something_else_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                stop() ;

                play();

            }
        });

        findViewById(R.id.manual_mode_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                //startActivity(new Intent(AutoActivity.this, ManualActvity.class));

                // because automode is second now
                finish();


            }
        });

        findViewById(R.id.save_this_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                mJam.finishHard();
                headBob.finish();
                save();
            }
        });

        findViewById(R.id.leibniz_head).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mJam.finishHard();
                headBob.restart();
                mJam.play();
            }
        });


        headBob = new HeadBob(this);

    }


    @Override
	public void onResume(){
		super.onResume();

        if (backFromContacts) {
            backFromContacts = false;
        }
        else {
            play();
        }

    }

    @Override
	public void onPause(){
		super.onPause();

        mJam.finish();

        headBob.finish();
    }


    @Override
    protected void onActivityResult(int req, int result, Intent data) {

        if (req == CONTACT_CHOOSER) {
            backFromContacts = true;

            if (result == RESULT_OK) {

                saveAudio.saveToContact(data);
            }
        }

    }

    private JamListener mJamListener = new JamListener() {

        public void onFinish() {

            headBob.finish();

        }

    };


    private void play() {
        // create a jam
        mJam = new TonezartJam(this);
        mJam.setJamListener(mJamListener);

        headBob.start(mJam.getBeatMs());

    }


    private void stop() {
        mJam.finish();
        headBob.finish();
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

                    mJam.finishHard();
                    mJam.play();

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

        mJam.finishHard();

        String name = ((EditText)dl.findViewById(R.id.txtOjName)).getText().toString();
        if (name.length() == 0)
            name = Long.toString(System.currentTimeMillis()) ;

        dl.hide();

        saveAudio = new SaveAudio(AutoActivity.this, mJam);
        saveAudio.copyWhenFinished(saveMode, name);
        saveAudio.execute();

    }

}
