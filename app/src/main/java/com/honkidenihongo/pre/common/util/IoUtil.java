package com.honkidenihongo.pre.common.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.honkidenihongo.pre.common.config.Definition;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Util class related to common operations of IO.
 *
 * @author long.tt.
 * @since 01-Dec-2016.
 */
public class IoUtil {
    /**
     * The Tag for logging.
     */
    private static String LOG_TAG = IoUtil.class.getSimpleName();

    /**
     * Buffer length.
     */
    private static final int BUFFER_LENGTH = 4096;

    /**
     * Value min of memory available space=60Mb.
     */
    public final static long VALUE_MEMORY_DOWNLOAD_MIN = 60;

    /**
     * Number of bytes in one KB = 2<sup>10</sup>
     */
    private final static long SIZE_KB = 1024L;

    /**
     * Number of bytes in one MB = 2<sup>20</sup>
     */
    public final static long SIZE_MB = SIZE_KB * SIZE_KB;

    /**
     * Key value error memory.
     */
    public final static int ERROR_MEMORY = 10;

    /**
     * Copy từ InputStream đến OutputStream.<br>
     * Chú ý: Dù kết quả thế nào đi nữa thì các tài nguyên IO vẫn không được giải phóng.
     *
     * @param inputStream  The InputStream.
     * @param outputStream The OutputStream.
     * @param bufferLength The buffer length.
     * @throws IOException Có khả năng xảy ra IOException và sẽ thực hiện ném ra bên ngoài exception đó.
     */
    public static void copy(InputStream inputStream, OutputStream outputStream, int bufferLength) throws IOException {
        byte[] bufferBytes = new byte[bufferLength];
        int count = 0;

        while ((count = inputStream.read(bufferBytes)) >= 0) {
            outputStream.write(bufferBytes, 0, count);
        }
    }

    /**
     * Copy từ InputStream đến OutputStream.{@link #copy(InputStream, OutputStream, int)}<br>
     * Chú ý: Dù kết quả thế nào đi nữa thì các tài nguyên IO vẫn không được giải phóng.
     *
     * @param inputStream  The InputStream.
     * @param outputStream The OutputStream.
     * @throws IOException Có khả năng xảy ra IOException và sẽ thực hiện ném ra bên ngoài exception đó.
     */
    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        copy(inputStream, outputStream, BUFFER_LENGTH);
    }

    /**
     * Read a file to byte array
     *
     * @param filePath path of file to read
     * @return Return byte array data
     */
    public static byte[] readFileToBytes(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            int len = (int) file.length();
            byte[] data = new byte[len];
            int count, total = 0;
            while ((count = fis.read(data, total, (len - total))) > 0) {
                total += count;
            }
            fis.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get mime type of file
     *
     * @param context context to get content resolver
     * @param uri     uri of file to get mime type
     * @return Return mime type of file
     */
    public static String getMimeType(Context context, Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    /**
     * Convert file to bytes data
     *
     * @param context context to get content resolver
     * @param uri     uri of file to converted
     * @return Return bytes data
     */
    public static byte[] getBytesFromUri(Context context, Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];

            int len;
            while ((len = is.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            return byteBuffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy tên file bỏ đi phần đuôi file ngoài cùng nếu có.<br>
     * {@link #getFileNameWithoutExtension(String)}
     *
     * @param file Đối tượng File.
     * @return Tên file sau khi đã khử phần mở rộng.
     */
    @Nullable
    public static String getFileNameWithoutExtension(File file) {
        // Lấy tên file zip bao gồm cả phần mở rộng.
        String fileName = file.getName();

        return getFileNameWithoutExtension(fileName);
    }

    /**
     * Lấy tên file bỏ đi phần đuôi file ngoài cùng nếu có.<br>
     * Ví dụ:<br>
     * <ul>
     * <li>Thông thường: filename1.zip -> filename1</li>
     * <li>Có 2 dấu chấm: filename1.filename2.zip -> filename1.filename2</li>
     * <li>Có nhiều dấu chấm liên tiếp: filename1.filename2...zip -> filename1.filename2..</li>
     * <li>Có dấu chấm cuối: filename1. -> filename1</li>
     * <li>Có nhiều dấu chấm cuối liên tiếp: filename1... -> filename1..</li>
     * <li>Không có dấu chấm: filename1 -> filename1</li>
     * <li>Có dấu chấm đầu: không hợp lệ: .zip-> null</li>
     * </ul>
     *
     * @param fileName Tên file bao gồm cả phần mở rộng.
     * @return Tên file sau khi đã khử phần mở rộng.
     */
    @Nullable
    public static String getFileNameWithoutExtension(String fileName) {
        // Khử phần mở rộng (nếu có), chỉ lấy tên file mà không có phần mở rộng.
        final String EXTENSION_SEPARATOR = ".";
        int separatorPosition = fileName.lastIndexOf(EXTENSION_SEPARATOR);

        // Nếu dấu chấm (extension separator) đứng đầu tức là không hợp lệ thì trả về null;
        if (separatorPosition == 0) {
            return null;
        }

        // Khởi tạo (việc này xảy ra khi không có dấu chấm nào).
        String fileNameWithoutExtension = fileName;

        // Nếu tồn tại dấu chấm thì khử phần mở rộng cuối cùng.
        if (separatorPosition > 0) {
            fileNameWithoutExtension = fileName.substring(0, separatorPosition);
        }

        return fileNameWithoutExtension;
    }

    /**
     * Read a text file to String
     *
     * @param jsonFilePath path of file
     * @return Return a string that is content of file
     */
    public static String readFileFromInternalStorageToString(Context context, String jsonFilePath) {
        File file = new File(context.getFilesDir(), jsonFilePath);
//        File file = new File(Environment.getExternalStorageDirectory(), jsonFilePath);
        if (!file.exists()) {
            Log.e(LOG_TAG, "File is not exists!");
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Unzip a zip file to specific directory using AsyncTask
     *
     * @param srcFileInfo        info of zip file to unzip
     * @param desFilePath        destination directory after unzip file
     * @param handleFileCallback Callback to handle some work on unzip processing
     * @return Return destination directory of unzip package
     */
    public static void unzip(final Context context, final Map<String, String> srcFileInfo, final String desFilePath,
                             final HandleFileCallback<Integer, Map<String, String>> handleFileCallback) {
        new AsyncTask<Void, Integer, Map<String, String>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (handleFileCallback != null) {
                    handleFileCallback.onPreExecute();
                }
            }

            @Override
            protected Map<String, String> doInBackground(Void... voids) {
                long START_TIME = System.currentTimeMillis();
                long FINISH_TIME;
                long ELAPSED_TIME;

                if (srcFileInfo == null) {
                    return null;
                }

                String srcFilePath = srcFileInfo.get(Definition.General.FILE_PATH);
                String date = srcFileInfo.get(Definition.General.DATE);

                Log.i(LOG_TAG, "ZipSource: " + srcFilePath);

                File mDir = context.getFilesDir();
//                File mDir = Environment.getExternalStorageDirectory();

                File srcFile = new File(mDir, srcFilePath);
                File desFile = new File(mDir, desFilePath);

                String desDirectory;
                if (!desFilePath.endsWith("/")) {
                    desDirectory = desFilePath + "/";
                } else {
                    desDirectory = desFilePath;
                }

                if (!srcFile.exists()) {
                    return null;
                }

                if (!desFile.exists()) {
                    desFile.mkdirs();
                }

                Map<String, String> result = new HashMap<>();
                result.put(Definition.General.DATE, date);
                try {
                    ZipFile zipFile = new ZipFile(srcFile);
                    final int totalEntry = zipFile.size();
                    if (handleFileCallback != null) {
                        handleFileCallback.onMaxProgress(totalEntry);
                    }
                    int countUnzip = 0;
                    Enumeration zipFileEntries = zipFile.entries();
                    byte buffer[] = new byte[4096];
                    int bytesRead;

                    while (zipFileEntries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                        if (entry.isDirectory()) {
                            File dir = new File(desFile, entry.getName());
                            if (!dir.exists()) {
                                dir.mkdir();
                            }
                            Log.i(LOG_TAG, "Unzip DIR: " + entry.getName());
                        } else {
                            File entryFile = new File(mDir, desDirectory + entry.getName());
                            entryFile.setReadable(true);
                            File entryParentFile = entryFile.getParentFile();
                            if (!entryParentFile.exists()) {
                                entryParentFile.mkdirs();
                            }
                            FileOutputStream fos = new FileOutputStream(entryFile);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                            while ((bytesRead = is.read(buffer)) != -1) {
                                baos.write(buffer, 0, bytesRead);
                            }

                            byte[] bytes = baos.toByteArray();
                            fos.write(bytes);
                            fos.close();
                            baos.close();
                            is.close();

                            countUnzip++;
                            publishProgress(countUnzip);

                            Log.i(LOG_TAG, "Unzip FILE: " + entry.getName());
                        }
                    }

                    FINISH_TIME = System.currentTimeMillis();
                    ELAPSED_TIME = FINISH_TIME - START_TIME;
                    Log.i(LOG_TAG, "COMPLETED in " + (ELAPSED_TIME / 1000) + " seconds.");
                    int lastIndex = srcFile.getName().lastIndexOf(".");
                    String unzipDir;
                    if (lastIndex != -1) {
                        unzipDir = desDirectory + srcFile.getName().substring(0, lastIndex);
                    } else {
                        unzipDir = desDirectory + srcFile.getName();
                    }

                    result.put(Definition.General.FILE_PATH, unzipDir);

                    return result;
                } catch (ZipException ze) {
                    ze.printStackTrace();
                    Log.e(LOG_TAG, "UNZIP FAILED");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    Log.e(LOG_TAG, "UNZIP FAILED");
                } finally {
                    boolean isDel = srcFile.delete();
                    Log.d(LOG_TAG, "DELETE FILE AFTER UNZIP: " + isDel);
                }
                return result;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if (handleFileCallback != null) {
                    handleFileCallback.onProgressUpdate(values);
                }
            }

            @Override
            protected void onPostExecute(Map<String, String> s) {
                super.onPostExecute(s);
                if (handleFileCallback != null) {
                    handleFileCallback.onPostExecute(s);
                }
            }
        }.execute();
    }

    /**
     * Download a file from to url and save to specific directory using AsyncTask
     *
     * @param stringUrl          String url of file which need to download
     * @param desDirectory       Directory to save file after download complete
     * @param handleFileCallback Callback to handle some work on download processing
     */
    public static void downloadFileFromUrl(
            final Context context, final String access_token, final String stringUrl,
            final String desDirectory, final HandleFileCallback<Integer, Map<String, String>> handleFileCallback) {
        new AsyncTask<Void, Integer, Map<String, String>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (handleFileCallback != null) {
                    handleFileCallback.onPreExecute();
                }
            }

            @Override
            protected Map<String, String> doInBackground(Void... voids) {
                File mDir = context.getFilesDir();
//                File mDir = Environment.getExternalStorageDirectory();
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                String desDirectoryFinal;
                if (!desDirectory.endsWith("/")) {
                    desDirectoryFinal = desDirectory + "/";
                } else {
                    desDirectoryFinal = desDirectory;
                }
                // Create directory in internal storage if not exists
                File desDirectoryFile = new File(mDir, desDirectoryFinal);
                if (!desDirectoryFile.exists()) {
                    desDirectoryFile.mkdirs();
                }

                Map<String, String> result = null;
                try {
                    URL url = new URL(stringUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty(Definition.Request.HEADER_AUTHORIZATION, Definition.Request.HEADER_BEARER + access_token);
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e(LOG_TAG, "Download Error: " + "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage());
                        return null;
                    }

                    result = new HashMap<>();

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    // Get file name
                    String fileName = "";
                    String disposition = connection.getHeaderField("Content-Disposition");
                    String date = connection.getHeaderField("Archive-Created-At");

                    if (disposition != null) {
                        // extracts file name from header field
                        int index = disposition.indexOf("filename=");
                        if (index > 0) {
                            fileName = disposition.substring(index + 10,
                                    disposition.length() - 1);
                        }
                    } else {
                        // extracts file name from URL
                        fileName = stringUrl.substring(stringUrl.lastIndexOf("/") + 1,
                                stringUrl.length());
                    }


                    String fileDesPath = desDirectoryFinal + fileName;
                    File desFile = new File(mDir, fileDesPath);

                    // download the file
                    input = connection.getInputStream();
                    output = new FileOutputStream(desFile);

                    if (handleFileCallback != null) {
                        handleFileCallback.onMaxProgress(100);
                    }

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;
                        // publishing the progress....
                        // only if total length is known
                        if (fileLength > 0) {
                            publishProgress((int) (total * 100 / fileLength));
                        }
                        output.write(data, 0, count);
                    }


                    result.put(Definition.General.FILE_PATH, fileDesPath);
                    result.put(Definition.General.DATE, date);

                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return result;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if (handleFileCallback != null) {
                    handleFileCallback.onProgressUpdate(values);
                }
            }

            @Override
            protected void onPostExecute(Map<String, String> s) {
                super.onPostExecute(s);
                if (handleFileCallback != null) {
                    handleFileCallback.onPostExecute(s);
                }
            }

        }.execute();
    }

    public interface HandleFileCallback<Progress, Result> {
        void onPreExecute();

        void onMaxProgress(int max);

        void onProgressUpdate(Progress... progress);

        void onPostExecute(Result result);
    }

    /**
     * Get total space.
     *
     * @param dir Thư mục đại diện ở Internal.
     * @return The available bytes.
     */
    public static long totalSpace(File dir) {
        StatFs statFs = new StatFs(dir.getAbsolutePath());

        long totalBytes;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // Get size of the block.
            long blockSize = (long) statFs.getBlockSize();

            // Total space in bytes.
            totalBytes = (long) statFs.getBlockCount() * blockSize;
        } else {
            // Total space in bytes.
            totalBytes = statFs.getTotalBytes();
        }

        return totalBytes;
    }

    /**
     * Get available space.
     *
     * @param dir Thư mục đại diện ở Internal.
     * @return The available bytes.
     */
    public static long availableSpace(File dir) {
        StatFs statFs = new StatFs(dir.getAbsolutePath());

        long availableBytes = -1;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // Get size of the block.
            int blockSize = statFs.getBlockSize();

            // Available space in bytes.
            availableBytes = ((long) statFs.getAvailableBlocks()) * blockSize;
        } else {
            // Available space in bytes.
            availableBytes = statFs.getAvailableBytes();
        }

        return availableBytes;
    }

    /**
     * Method using copy content of folder old to new folder.
     *
     * @param sourceLocation Folder old.
     * @param targetLocation New folder.
     * @throws IOException
     */
    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();

            for (String aChildren : children) {
                copyDirectory(new File(sourceLocation, aChildren), new File(targetLocation, aChildren));
            }
        } else {

            // Make sure the directory we plan to store the recording in exists.
            File directory = targetLocation.getParentFile();

            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outStream.
            byte[] buf = new byte[BUFFER_LENGTH];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
        }
    }

    /**
     * Method using delete folder and content of it.
     *
     * @param file Value name of folder be delete.
     * @return Ok or not.
     */
    public static boolean deleteDirectory(File file) {
        if (file.exists()) {
            File[] files = file.listFiles();

            if (files == null) {
                return true;
            }

            for (File fileChild : files) {
                if (fileChild.isDirectory()) {
                    deleteDirectory(fileChild);
                } else {
                    fileChild.delete();
                }
            }
        }

        return file.delete();
    }
}
