package com.honkidenihongo.pre.common.util;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Util class related to common operations of System.
 *
 * @author long.tt.
 * @since 17-Nov-2016.
 */
public class ZipUtil {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = ZipUtil.class.getName();

    /**
     * The private constructor to prevent creating object.
     */
    private ZipUtil() {
    }

    /**
     * Unzip file đến thư mục có tên trùng với tên file zip (bỏ đi phần đuôi file ngoài cùng nếu có).<br>
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
     * Tham khảo: {@link #unzipFile(File, File)}.
     *
     * @param zipFile Đối tượng file zip cần unzip.
     * @return Đối tượng thư mục sau khi unzip.
     */
    @Nullable
    public static File unzipFile(File zipFile) {
        // Lấy đường dẫn tới thư mục gốc chứa file zip.
        String baseDirPath = zipFile.getParent();

        // Khử phần mở rộng (nếu có), chỉ lấy tên file mà không có phần mở rộng.
        String fileNameWithoutExtension = IoUtil.getFileNameWithoutExtension(zipFile);

        // Nếu không lấy được tên hợp lệ thì trả về null.
        if (TextUtils.isEmpty(fileNameWithoutExtension)) {
            // Show the log in development environment.
            Log.d(LOG_TAG, "Cannot get the file name without extension.");

            return null;
        }

        // Tạo thư mục đích sẽ unzip tới.
//        String destinationDirPath = baseDirPath + File.separator + fileNameWithoutExtension;
        File outputDir = new File(baseDirPath, fileNameWithoutExtension);

        // Nếu thư mục đích sẽ unzip tới không tồn tại thì tạo. Nếu tạo không thành công thì trả về null.
        if (!outputDir.exists()) {
            try {
                boolean createDirResult = outputDir.mkdirs();

                if (!createDirResult) {
                    // Show the log in development environment.
                    Log.d(LOG_TAG, "unzipFile(): Cannot create directory.");

                    return null;
                }
            } catch (Exception ex) {
                // Show the log in development environment.
                Log.e(LOG_TAG, "unzipFile(): " + ex.getMessage());

                return null;
            }
        }

        return unzipFile(zipFile, outputDir);
    }

    /**
     * Unzip the file to the directory.
     *
     * @param zipFile   Đối tượng file zip cần unzip.
     * @param outputDir Thư mục sẽ unzip tới. Thư mục này phải tồn tại rồi.
     * @return Nếu thành công thì trả về đối tượng thư mục đã được unzip tới, còn nếu có lỗi thì trả về null.
     */
    @Nullable
    public static File unzipFile(File zipFile, File outputDir) {
        // Validate thư mục: Nếu không phải thư mục hoặc là thư mục nhưng không tồn tại thì return null.
        if (!outputDir.isDirectory() || !outputDir.exists()) {
            return null;
        }

        // Init ZipInputStream object.
        ZipInputStream zis = null;

        final int BUFFER_LENGTH = 4096;

        try {
            // Tạo ZipInputStream.
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), BUFFER_LENGTH));
            ZipEntry zipEntry = null;

            while ((zipEntry = zis.getNextEntry()) != null) {
                // Lấy tên của Zip Entry (đường dẫn tương đối của file hoặc thư mục).
                String entryName = zipEntry.getName();
                Log.d(LOG_TAG, "Unzipping entry: " + entryName);

                // Nếu Zip Entry là thư mục thì tạo nó nếu không tồn tại trước đó.
                if (zipEntry.isDirectory()) {
                    File entryDir = new File(outputDir, entryName);

                    // Nếu tạo thư mục OK thì lặp tiếp các Entry khác.
                    if (!entryDir.exists()) {
                        try {
                            boolean createDirResult = entryDir.mkdirs();

                            if (!createDirResult) {
                                // Show the log in development environment.
                                Log.d(LOG_TAG, "unzipFile(): Cannot create directory.");

                                return null;
                            }
                        } catch (Exception ex) {
                            // Show the log in development environment.
                            Log.e(LOG_TAG, "unzipFile(): " + ex.getMessage());

                            return null;
                        } finally {
                            try {
                                // Close Entry.
                                zis.closeEntry();
                            } catch (Exception ex) {
                                Log.d(LOG_TAG, "unzipFile(): " + ex.getMessage());
                            }
                        }
                    }
                }

                // Đến đây thì các Entry là File hoặc đường dẫn tương đối đến File nên sẽ tiến hành copy ra.
                FileOutputStream fos = null;

                try {
                    File entryFile = new File(outputDir, entryName);

                    // Tạo thư mục nếu theo đường dẫn tương đối tới file nếu nó chưa tồn tại.
                    File parentDir = entryFile.getParentFile();

                    if (!parentDir.exists()) {
                        boolean createDirResult = parentDir.mkdirs();

                        if (!createDirResult) {
                            // Show the log in development environment.
                            Log.d(LOG_TAG, "unzipFile(): Cannot create directory.");

                            return null;
                        }
                    }

                    fos = new FileOutputStream(entryFile);

                    // Thực hiện copy.
                    IoUtil.copy(zis, fos);
                } catch (Exception ex) {
                    // Show the log in development environment.
                    Log.e(LOG_TAG, "unzipFile(): " + ex.getMessage());

                    return null;
                } finally {
                    try {
                        // Close IO resources.
                        if (fos != null) {
                            fos.close();
                        }

                        zis.closeEntry();
                    } catch (Exception ex) {
                        Log.d(LOG_TAG, "unzipFile(): " + ex.getMessage());
                    }
                }
            }

            return outputDir;
        } catch (Exception ex) {
            // Show the log in development environment.
            Log.e(LOG_TAG, "unzipFile(): " + ex.getMessage());

            return null;
        } finally {
            if (zis != null) {
                try {
                    // Close IO resources.
                    zis.close();
                } catch (Exception ex) {
                    Log.d(LOG_TAG, "unzipFile(): " + ex.getMessage());
                }
            }
        }
    }

}
