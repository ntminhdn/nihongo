package com.honkidenihongo.pre.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {

    private static final String LOG_TAG = BitmapUtils.class.getSimpleName();

    // Max size of photo iss 1Mb.
    private static final int IMAGE_MAX_SIZE = 1024;

    /**
     * Calculator inSampleSize value to decode Bitmap
     *
     * @param options   options to decode bitmap
     * @param reqWidth  require width dimen to decode bitmap
     * @param reqHeight require height dimen to decode bitmap
     * @return Return new integer value compatible with requirement
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Decode bitmap from resource to avoid out of memory
     *
     * @param context   context to get resource
     * @param res_id    resource id of image to decode
     * @param reqWidth  require width dimen to decode bitmap
     * @param reqHeight require height dimen to decode bitmap
     * @return Bitmap compatible with requirement
     */
    public static Bitmap decodeBitmapFromResource(Context context, int res_id, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), res_id, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(context.getResources(), res_id, options);
    }

    /**
     * Decode bitmap from Uri to avoid out of memory
     *
     * @param context   context to get bytes
     * @param uri       Uri of image to decode
     * @param reqWidth  require width dimen to decode bitmap
     * @param reqHeight require height dimen to decode bitmap
     * @return Bitmap compatible with requirement
     */
    public static Bitmap decodeBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) {
        byte[] bytes = IoUtil.getBytesFromUri(context, uri);
        if (bytes == null) {
            return null;
        }
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /**
     * Decode bitmap from file which be stored in internal storage
     *
     * @param context   context to get root directory of internal storage
     * @param filePath  path of image file in internal storage to decode
     * @param reqWidth  require width dimen to decode bitmap
     * @param reqHeight require height dimen to decode bitmap
     * @return Bitmap compatible with requirement
     */
    public static Bitmap decodeBitmapFromFileInternal(Context context, String filePath, int reqWidth, int reqHeight) {
        Log.d(LOG_TAG, "ImagePath: " + filePath);
        File file = new File(context.getFilesDir(), filePath);
//        File file = new File(Environment.getExternalStorageDirectory(), filePath);
        if (!file.exists()) {
            Log.e(LOG_TAG, "Image file not exists!");
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            fis.close();
            byte[] bytes = baos.toByteArray();
            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Uri getImageUri(String path) {
        return Uri.fromFile(new File(path));
    }

    /**
     * Method using get decode image size from uri.
     *
     * @param context Value context of screen current.
     * @param uri     Value uri of photo.
     * @return Bitmap.
     */
    public static Bitmap getBitmap(Context context, Uri uri) {
        InputStream in = null;
        Bitmap returnedBitmap = null;

        try {
            in = context.getContentResolver().openInputStream(uri);
            // Decode image size.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            int scale = 1;

            if (options.outHeight > IMAGE_MAX_SIZE || options.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options options1 = new BitmapFactory.Options();
            options1.inSampleSize = scale;
            in = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options1);
            in.close();

            // First check.
            ExifInterface ei = new ExifInterface(uri.getPath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    returnedBitmap = rotateImage(bitmap, 90);

                    // Free up the memory.
                    bitmap.recycle();

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    returnedBitmap = rotateImage(bitmap, 180);

                    // Free up the memory.
                    bitmap.recycle();

                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    returnedBitmap = rotateImage(bitmap, 270);

                    // Free up the memory.
                    bitmap.recycle();

                    break;

                default:
                    returnedBitmap = bitmap;
            }

            return returnedBitmap;

        } catch (IOException e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return null;
    }

    /**
     * Method using rotate photo.
     *
     * @param bitmap Value bitmap current.
     * @param angle  Value angle.
     * @return Bitmap.
     */
    private static Bitmap rotateImage(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
