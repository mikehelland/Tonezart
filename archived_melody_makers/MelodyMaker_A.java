package com.monadpad.tonezart;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * User: m
 * Date: 9/29/13
 * Time: 9:32 PM
 */
public class MelodyMaker {

    Random rand;
    Context mContext;

    int key;

    public MelodyMaker(Context context) {
        mContext = context;

        rand = new Random();

    }

    public List<TonezartNote> makeMeAMelody(Context context) {
        List<List<TonezartNote>> notes = new ArrayList<List<TonezartNote>>();

        // 4/4
        float beats = 4.0f;

        String form = forms[rand.nextInt(forms.length)];

        Log.d("MGH form", form);

        float beatsPerPart = (4 / form.length()) * beats;

        // pick a key
        String[] keyCaptions = context.getResources().getStringArray(R.array.base_captions);
        String[] keys = context.getResources().getStringArray(R.array.base);
        int keyIndex = rand.nextInt(keys.length);

        key = 24 + Integer.parseInt(keys[keyIndex]);
        String keyCaption = keyCaptions[keyIndex];
        Log.d("MGH bars", keyCaption);

        for (int ipart = 0; ipart < form.length(); ipart++) {

            String part = form.substring(ipart, ipart + 1);
            Log.d("MGH part name", part);

            if (form.indexOf(part) < ipart) {

                List<TonezartNote> partNotes = new ArrayList<TonezartNote>();
                for (TonezartNote note : notes.get(form.indexOf(part))) {
                    partNotes.add(note.clone());
                }
                notes.add(partNotes);

            }
            else {
                notes.add(makeAPart(beatsPerPart));
            }

        }

        List<TonezartNote> allNotes = new ArrayList<TonezartNote>();
        for (List<TonezartNote> partNotes : notes) {
            for (TonezartNote note : partNotes) {
                allNotes.add(note);
            }
        }
        return allNotes;
    }

    private List<TonezartNote> makeAPart(float totalBeats) {

        int restRatio = 2 + rand.nextInt(10);

        List<TonezartNote> notes = new ArrayList<TonezartNote>();

        float playedBeats = 0.0f;
        float currentNoteDuration;
        int currentNoteNumber;
        TonezartNote currentNote;

        int lastNote = key;

        int notesAway;

        boolean goingUp = rand.nextBoolean();

        while (playedBeats < totalBeats) {

            currentNote = new TonezartNote();

            notes.add(currentNote);

            currentNoteDuration = Math.min(getRandomNoteDuration(),
                    totalBeats - playedBeats);
            currentNote.setDuration(currentNoteDuration);

            playedBeats += currentNoteDuration;

            // rest?
            if (rand.nextInt(restRatio) == 0) {
                currentNote.setNote(-1);
                continue;
            }

            // play the last note
            if (rand.nextBoolean()) {
                currentNote.setNote(lastNote);
                continue;
            }

            // maybe change the direction
            if (rand.nextBoolean()) {
                goingUp = rand.nextBoolean();
            }

            // play a different note
            notesAway = rand.nextBoolean() ? 1 : rand.nextBoolean() ? 2 : 3;

            if (!goingUp) {
                notesAway = notesAway * -1;
            }
            currentNoteNumber = lastNote + notesAway;
            currentNote.setNote(currentNoteNumber);
            lastNote = currentNoteNumber;
        }

        return notes;
    }

    public float getRandomNoteDuration() {

        // 50 50 chance we get an eighth note
        if (rand.nextBoolean())
            return 0.5f;

        // go for a sixteenth
        if (rand.nextBoolean())
            return 0.25f;

        // try and eighth note again
        if (rand.nextBoolean())
            return 0.5f;



        // resort to quarter note
        return 1.0f;

    }

//    String[] forms = {"A", "AA", "ABAB", "AAAB", "ABAC" };
    String[] forms = {"AAAA" };
}
