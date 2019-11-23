package ru.fagci.tuihome.loader;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import ru.fagci.tuihome.model.ContactModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactLoaderTask extends ModelLoaderTask {
    public ContactLoaderTask(Context context) {
        super(context);
    }

    @Override
    public List<ContactModel> loadInBackground() {
        List<ContactModel> entries = new ArrayList<>();
        final ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() == 0) return entries;

        while (cursor.moveToNext()) {
            if (isLoadInBackgroundCanceled()) break;
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) == 0) continue;

            Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

            Bitmap photo = null;
            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
            }

            while (cursorInfo.moveToNext()) {
                ContactModel info = new ContactModel(
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)),
                        cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                );

                if (null != photo) info.bitmap = Bitmap.createScaledBitmap(photo, 48, 48, false);

                entries.add(info);
            }

            cursorInfo.close();
        }
        cursor.close();
        return entries;
    }
}