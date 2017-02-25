package com.monadpad.tonezart;

/**
 * User: m
 * Date: 9/29/13
 * Time: 10:19 PM
 */
public class TonezartNote {

    private double duration;
    int note;
    private long placeInLine;
    boolean accented = false;
    private boolean rest = false;

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public int getNote() {
        return note;
    }

    public double getDuration() {
        return duration;
    }

    public void setPlaceInLine(long place) {
        placeInLine = place;
    }
    public long getPlaceInLine() {
        return placeInLine;
    }

    public TonezartNote clone() {
        TonezartNote newNote = new TonezartNote();
        newNote.setDuration(duration);
        newNote.setNote(note);
        newNote.rest = rest;
        return newNote;
    }

    public TonezartNote cloneSlowVersion() {
        TonezartNote newNote = new TonezartNote();
        newNote.setDuration(duration * 2.0f);
        newNote.setNote(note > 0 ? note + 12 : -1);
        return newNote;
    }

    public void setRest(boolean rest) {
        this.rest = rest;
    }

    public boolean isRest() {
        return rest;
    }
}
