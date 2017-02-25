package com.monadpad.tonezart;

import java.util.ArrayList;
import java.util.List;

/**
 * User: m
 * Date: 9/30/13
 * Time: 5:52 AM
 */
public class NoteLine {

    private List<TonezartNote> mNotes = new ArrayList<TonezartNote>();

    private int durationMs;

    private int instrument;

    public List<TonezartNote> getNotes() {
        return mNotes;
    }


    public NoteLine prepareNotes(int beatMs) {

        int marker = 0;
        TonezartNote note;
        for (int i = 0; i < mNotes.size(); i++) {

            note = mNotes.get(i);
            note.setPlaceInLine(marker);

            marker += beatMs * note.getDuration();

        }

        durationMs = marker;

        return this;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public void setInstrument(int instrument) {
        this.instrument = instrument;
    }

    public int getInstrument(){
        return instrument;
    }
}
