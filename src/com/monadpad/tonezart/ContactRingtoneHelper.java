package com.monadpad.tonezart;

/**
 * User: m
 * Date: 9/29/13
 * Time: 9:26 PM
 */
public class ContactRingtoneHelper {

    public void setRingtoneForContact() {
/*
        try {
            Uri contactData = data.getData();
            String cId = contactData.getLastPathSegment();

            String[] PROJECTION = new String[] {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
            };
            Cursor cur = getContentResolver().query(contactData, PROJECTION, null, null, null);
            cur.moveToFirst();

            String contactID = cur.getString(cur.getColumnIndexOrThrow("_id"));
            String contactDisplayName = cur.getString(cur.getColumnIndexOrThrow("display_name"));

            Uri localUrl = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactID);
            cur.close();
            ContentValues values = new ContentValues();

            values.put(ContactsContract.Data.RAW_CONTACT_ID, cId);
//todo                values.put(ContactsContract.Data.CUSTOM_RINGTONE, mpad.getLastSavedRingtone().toString());

            getContentResolver().update(localUrl, values, null, null);
            Toast.makeText(this, "Ringtone assigned to " + contactDisplayName, Toast.LENGTH_LONG).show();

        }
        catch (Exception e) {

            Log.d("MGH exception thingy", e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();

        }

    */
    }

}
