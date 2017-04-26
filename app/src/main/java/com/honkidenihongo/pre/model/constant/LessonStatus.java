package com.honkidenihongo.pre.model.constant;

/**
 * Hằng số thể hiện các kiểu trạng thái của Lesson như: UN_DOWNLOADED, DOWNLOADING, DOWNLOADED, LEARN_COMPLETED,
 *
 * @author long.tt
 * @since 16-Nov-2016.
 */
public final class LessonStatus {
    /**
     * The private constructor to prevent creating new object.
     */
    private LessonStatus() {
    }

    /**
     * Lesson Status is Init.
     */
    public static final int UN_DOWNLOADED = 0;

    /**
     * Lesson Status is Waiting.
     */
    public static final int WAITING = 1;

    /**
     * Lesson Status is Downloading.
     */
    public static final int DOWNLOADING = 2;

    /**
     * Lesson Status is Download Error.
     */
    public static final int DOWNLOAD_ERROR = 3;

    /**
     * Lesson Status is Downloaded.
     */
    public static final int DOWNLOADED = 4;

    /**
     * Lesson Status is update new version.
     */
    public static final int UPDATE = 5;

    /**
     * Lesson Status is updating new version.
     */
    public static final int UPDATING = 6;

    /**
     * Lesson Status is waiting update.
     */
    public static final int WAITING_UPDATE = 7;

    /**
     * Lesson Status is Learn Completed.
     */
    public static final int LEARN_COMPLETED = 9;

}
