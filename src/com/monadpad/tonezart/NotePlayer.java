package com.monadpad.tonezart;

import java.util.List;

/**
 * User: m
 * Date: 9/29/13
 * Time: 10:47 PM
 */
public class NotePlayer {

    MonadaphoneChannel mpc;

    private int i = 0;

    private NoteLine mLine;

    private boolean muted = false;

    public NotePlayer(MonadaphoneChannel channel) {
        mpc = channel;
    }

    public void setLine(NoteLine line)  {
        mLine = line;
    }

    public void update(long now) {

        if (muted)
            return;

        if (mLine == null)
            return;

        List<TonezartNote> notes = mLine.getNotes();

        if (notes.size() <= i)
            return;

        TonezartNote note = notes.get(i);

        if (note.getPlaceInLine() <= now % mLine.getDurationMs()) {
            mpc.playNote(note.isRest() ? -1 : note.getNote());
            i++;
        }


    }

    public void resetI() {
        if (mLine != null && i == mLine.getNotes().size())
            i = 0;
    }


    public void mute() {
        muted = true;
        mpc.playNote(-1);
    }

    public void unmute() {
        muted = false;
        //mpc.unmute();
        //mpc.playNote(-1);
    }

}
