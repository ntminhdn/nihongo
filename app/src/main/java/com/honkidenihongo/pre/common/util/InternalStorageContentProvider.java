package com.honkidenihongo.pre.common.util;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.honkidenihongo.pre.gui.auth.S04UserProfile_Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * Reference: The solution is taken from here: http://stackoverflow.com/questions/10042695/how-to-get-camera-result-as-a-uri-in-data-folder
 *
 * @author binh.dt.
 * @since 16-Jan-2017.
 */
public class InternalStorageContentProvider extends ContentProvider {
    private static final String URI_APP_NAME = "content://com.honkidenihongo.pre.profile.crop/";
    private static final String LOG_TAG = InternalStorageContentProvider.class.getName();
    private static final HashMap<String, String> MIME_TYPES = new HashMap<>();
    public static final Uri CONTENT_URI = Uri.parse(URI_APP_NAME);

    // Todo cần định nghĩa trong file define.
    static {
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
    }

    @Override
    public boolean onCreate() {
        try {
            File mFile = new File(getContext().getFilesDir(), S04UserProfile_Fragment.TEMP_PHOTO_FILE_NAME);

            if (!mFile.exists()) {
                mFile.createNewFile();
                getContext().getContentResolver().notifyChange(CONTENT_URI, null);
            }

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            return false;
        }
    }

    @Override
    public String getType(Uri uri) {
        String path = uri.toString();

        for (String extension : MIME_TYPES.keySet()) {
            if (path.endsWith(extension)) {
                return (MIME_TYPES.get(extension));
            }
        }

        return null;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File f = new File(getContext().getFilesDir(), S04UserProfile_Fragment.TEMP_PHOTO_FILE_NAME);

        if (f.exists()) {
            return (ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_WRITE));
        }

        throw new FileNotFoundException(uri.getPath());
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
