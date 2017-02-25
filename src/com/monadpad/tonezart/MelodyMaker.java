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

    List<TonezartNote> currentMelody;

    private boolean madeBassLine = false;

    //    String[] forms = {"A", "AA", "ABAB", "AAAB", "ABAC" };
    String[] forms = {"AAAA" };

    float[] ascale;

    String keyName;

    private int instrumentCount;


    public MelodyMaker(Context context) {
        mContext = context;

        rand = new Random();

        // pick a key
        String[] keyCaptions = context.getResources().getStringArray(R.array.keys_captions);
        String[] keys = context.getResources().getStringArray(R.array.keys);
        int keyIndex = rand.nextInt(keys.length);

        // pick a scale
        String[] scales = context.getResources().getStringArray(R.array.quantizer_values);
        String[] scaleCaptions = context.getResources().getStringArray(R.array.quantizer_entries);
        int scaleIndex = rand.nextInt(scales.length - 2); // -2, ignore octave and thermin scale

        ascale = MonadaphoneChannel.buildScale(scales[scaleIndex]);

        keyName = keyCaptions[keyIndex] + " " + scaleCaptions[scaleIndex];

        key = 12 + Integer.parseInt(keys[keyIndex]);

        instrumentCount = context.getResources().getStringArray(R.array.instruments).length;
    }

    public List<TonezartNote> makeSections() {
        List<List<TonezartNote>> notes = new ArrayList<List<TonezartNote>>();

        // 4/4
        float beats = 4.0f;

        String form = forms[rand.nextInt(forms.length)];

        Log.d("MGH form", form);

        float beatsPerPart = (4 / form.length()) * beats;

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
                //todo notes.add(makeMelody(beatsPerPart));
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

    public NoteLine makeMelody(float totalBeats) {

        NoteLine line = new NoteLine();

        // choose a duration to tend toward
        double beatBias = (1 + rand.nextInt(3)) * 0.125;

        int adjust = 12;

        if (!madeBassLine && rand.nextInt(3) == 0) {
            madeBassLine = true;

            line.setInstrument(4 + rand.nextInt(instrumentCount - 6));

            adjust = 0;
        }
        else {
            line.setInstrument(rand.nextInt(instrumentCount));
        }

        int restRatio = 2 + rand.nextInt(10);


        float playedBeats = 0.0f;
        double currentNoteDuration;
        int currentNoteNumber;
        TonezartNote currentNote;

        int lastNote = key;

        int notesAway;

        boolean goingUp = rand.nextBoolean();

        while (playedBeats < totalBeats) {

            currentNote = new TonezartNote();

            line.getNotes().add(currentNote);

            currentNoteDuration = Math.min(getRandomNoteDuration(beatBias),
                    totalBeats - playedBeats);
            currentNote.setDuration(currentNoteDuration);

            playedBeats += currentNoteDuration;

            // rest?
            if (rand.nextInt(restRatio) == 0) {
                currentNote.setRest(true);
                continue;
            }

            // play the last note
            if (rand.nextBoolean()) {
                currentNote.setNote(adjust + lastNote);
                continue;
            }

            // maybe change the direction
            if (rand.nextBoolean()) {
                goingUp = rand.nextBoolean();
            }

            // play a different note
            notesAway = rand.nextBoolean() ? 1 : rand.nextBoolean() ? 2 : rand.nextBoolean() ? 3 : 1;

            if (!goingUp) {
                notesAway = notesAway * -1;
            }
            currentNoteNumber = lastNote + notesAway;
            currentNote.setNote(adjust + currentNoteNumber);
            lastNote = currentNoteNumber;


            Log.d("MGH melody maker ",
                    "  current note: " + Integer.toString(currentNoteNumber));

        }

        Log.d("MGH melody maker notes", Integer.toString(line.getNotes().size()));


        // go backwards
        if (rand.nextInt(3) == 0) {
            NoteLine line2 = new NoteLine();
            line2.setInstrument(line.getInstrument());
            for (int ii = line.getNotes().size(); ii > 0; ii--) {
                line2.getNotes().add(line.getNotes().get(ii - 1));
            }
            line = line2;
        }

        currentMelody = line.getNotes();
        return line;
    }

    public NoteLine applyScale(NoteLine notes) {

        int oldNote;
        int octaves;

        NoteLine returnLine = new NoteLine();
        returnLine.setInstrument(notes.getInstrument());
        TonezartNote newNote;
        TonezartNote note;
        for (int i = 0; i < notes.getNotes().size(); i++) {

            note = notes.getNotes().get(i);

            octaves = 0;

            newNote = note.clone();
            oldNote = note.getNote() - key;
            returnLine.getNotes().add(newNote);

            if (newNote.isRest()) {
                continue;
            }

            while (oldNote >= ascale.length) {
                octaves++;
                oldNote = oldNote - ascale.length;
            }

            while (oldNote < 0) {
                octaves--;
                oldNote = oldNote + ascale.length;
            }

            newNote.setNote(key + (int)ascale[oldNote] + octaves * 12);

            Log.d("MGH melody maker apply scale", "Old note: " + Integer.toString(note.getNote() - key) +
                    "  New note: " + Integer.toString(newNote.getNote() - key));
        }

        return returnLine;
    }

    public double getRandomNoteDuration(double beatBias) {

        if (rand.nextBoolean() ) {
            return beatBias;
        }

        // 50 50 chance we get an eighth note
        if (rand.nextBoolean())
            return 0.5d;

        // go for a sixteenth
        if (rand.nextBoolean())
            return 0.25d;

        // try and eighth note again
        if (rand.nextBoolean())
            return 0.5d;


        return beatBias;

    }


    public NoteLine getSlowMelody() {
        NoteLine slowMelody = new NoteLine();

        for (int inote = currentMelody.size(); inote > 0; inote--) {
            slowMelody.getNotes().add(currentMelody.get(inote - 1).cloneSlowVersion());
        }

        return slowMelody;
    }

    public NoteLine getMelody() {

        NoteLine line = new NoteLine();
        TonezartNote newNote;
        for (TonezartNote note : currentMelody) {

            newNote = note.clone();

            line.getNotes().add(newNote);
        }
        return line;
    }

    public String getKeyName() {
        return keyName;
    }
}
