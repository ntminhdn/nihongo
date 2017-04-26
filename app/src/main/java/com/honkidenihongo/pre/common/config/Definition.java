package com.honkidenihongo.pre.common.config;

/**
 * Hằng số thể hiện các cấu hình dùng chung toàn ứng dụng.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public final class Definition {
    /**
     * The private constructor to prevent creating new object.
     */
    private Definition() {
    }

    /**
     * Hằng số thể hiện các kiểu ngôn ngữ (Japanese, Vietnamese, English) dưới dạng String.
     *
     * @author long.tt.
     * @since 16-Nov-2016.
     */
    public static final class LanguageCode {
        /**
         * The private constructor to prevent creating new object.
         */
        private LanguageCode() {
        }

        public static final String JAPANESE = "ja";
        public static final String VIETNAMESE = "vi";
        public static final String ENGLISH = "en";
    }

    /**
     * Hằng số thể hiện việc setting cho app.
     *
     * @author Binh.dt.
     * @since 23-Feb-2017.
     */
    public static final class SettingApp {
        /**
         * The private constructor to prevent creating new object.
         */
        private SettingApp() {
        }

        public static final String SETTING_APPLICATION = "setting_application";
        public static final String APP_CODE_VERSION = "app_code_version";

        /**
         * Class setting showDialog help.
         */
        public static class DialogHelp {

            /**
             * The private constructor.
             */
            private DialogHelp() {
            }

            // For application.
            public static final String SHOW_DIALOG_HELP_ALL_APPLICATION = "SHOW_DIALOG_HELP_ALL_APPLICATION";

            public static final String DIALOG_UPDATE_S03_DASH_BOARD = "DIALOG_UPDATE_S03_DASH_BOARD";
            public static final String DIALOG_HELP_S03_DASH_BOARD = "DIALOG_HELP_S03_DASH_BOARD";
            public static final String DIALOG_HELP_S04_USER_PROFILE = "DIALOG_HELP_S04_USER_PROFILE";
            public static final String DIALOG_HELP_S05_NAVIGATION_MENU = "DIALOG_HELP_S05_NAVIGATION_MENU";
            public static final String DIALOG_HELP_S07_LESSON_CATEGORY = "DIALOG_HELP_S07_LESSON_CATEGORY";
            public static final String DIALOG_HELP_S11_PRACTICE_LIST = "DIALOG_HELP_S11_PRACTICE_LIST";
            public static final String DIALOG_HELP_S13_RANKING = "DIALOG_HELP_S13_RANKING";
            public static final String DIALOG_HELP_S14_FLASH_CASH = "DIALOG_HELP_S14_FLASH_CASH";
            public static final String DIALOG_HELP_S15_KNOWLEDGE = "DIALOG_HELP_S15_KNOWLEDGE";
            public static final String DIALOG_HELP_S18_SETTING = "DIALOG_HELP_S18_SETTING";
            public static final String DIALOG_HELP_S19_LESSON_CATEGORY_CONTENT = "DIALOG_HELP_S19_LESSON_CATEGORY_CONTENT";
            public static final String DIALOG_HELP_S24_TRIAL_TEST = "DIALOG_HELP_S24_TRIAL_TEST";
            public static final String DIALOG_HELP_S27_GRAMMAR = "DIALOG_HELP_S27_GRAMMAR";
        }
    }

    /**
     * Define General
     */
    public class General {
        public static final String UNIT_ID = "unit_id";
        public static final String KNOWLEDGE_ID = "knowledge_id";
        public static final String QUESTION_ID = "question_id";
        public static final String LESSON_ID = "lesson_number";
        public static final String COURSE_ID = "course_id";
        public static final String UNIT_DATA_ID = "unit_data_id";
        public static final String CONTENT_DATA_ID = "content_data_id";
        public static final String HISTORY_ID = "history_id";
        public static final String CHARACTER = "character";
        public static final String MINNA1 = "minna1";
        public static final String VOCABULARY = "vocabulary";
        public static final String SENTENCE = "sentence";
        public static final String KNOWLEDGE = "knowledge";
        public static final String FLASHCARD = "flashcard";
        public static final String PRACTICE = "practice";
        public static final String TEST = "test";
        public static final String MP3_TYPE = ".mp3";
        public static final String AUDIO_DIR = "audio_directory";
        public static final String LEARNING_TYPE = "learning_type";
        public static final String LEARNING_DATA = "learning_data";
        public static final String TAB_ID = "tab_id";
        public static final String TEAM_ID = "team_id";
        public static final String TEAM_NAME = "team_name";
        public static final String RESULT = "result";
        public static final String POSITION = "position";
        public static final String MODE = "mode";
        public static final String COUNT = "count";
        public static final String WEEK = "week";
        public static final String PUSH_NOTIFICATION = "push_notification";
        public static final String INDEX = "index";
        public static final String FILE_PATH = "file_path";
        public static final String DATE = "date";
        public static final String BASIC = "basic";
        public static final String ADVANCE = "advance";
        public static final String BREADCRUMB_SEPARATOR = " > ";
    }

    /**
     * Class định nghĩa các value cho màn hình kết quả.
     */
    public static class Result {

        /**
         * The private constructor.
         */
        private Result() {
        }

        public static final double TIME_COMPLETED_ONE_QUESTION_MAX = 3; // Value time for user completed 1 question max is 3s.
        public static final String RESULT_FORMAT_TIME_COMPLETED = "%.2f";
        public static final String RESULT_FORMAT_TIME_POINT = "%.2f";
        public static final String SUFFIX_SECOND = "s";
        public static final String SUFFIX_MINUTE = "m";
        public static final String SUFFIX_HOURS = "h";
        public static final String SUFFIX_DAY = "d";
    }


    /** Define API */

    /**
     * Define json version cũ.
     * Nên dần dần chuyển sang file: {@link  DownloadFileConfig}.
     */
    public class FileData {
        public static final String DATA_DIRECTORY = "lll/data/";
        public static final String AUDIO_DIRECTORY = "assets/audio/";
        public static final String KNOWLEDGE_FILE = "knowledge.json";
        public static final String QUESTION_FILE = "question.json";
        public static final String MAIN_FILE = "main.json";
        public static final String UNIT_FILE = "unit.json";
        public static final String PRACTICE_RANKING_FILE = "practice_ranking.json";
        public static final String PRACTICE_FILE = "practice.json";
        public static final String TEST_FILE = "test.json";
        public static final String UNIT_CONTENT_PROGRESS_FILE = "unit_content_progress.json";
        public static final String UNIT_PROGRESS_FILE = "unit_progress.json";
    }

    /**
     * Define JSON Key
     */
    public class JSON {
        public static final String DATA_KEY = "data";
        public static final String FORMAT_KEY = "format";
        public static final String QUESTION_KEY = "question";
        public static final String AUDIO_KEY = "audio";
        public static final String ANSWERS_KEY = "answers";
        public static final String NAME_KEY = "name";
        public static final String VERSION_KEY = "version";
        public static final String SHORT_NAME_KEY = "shortName";
        public static final String UNIT_KEY = "unit";
        public static final String TYPE_KEY = "type";
        public static final String CONTENTS_KEY = "contents";
        public static final String ICON_KEY = "icon";
        public static final String TITLE_KEY = "title";
        public static final String DATE_KEY = "date";
        public static final String COURSE_ID_KEY = "course_id";
        public static final String UNIT_ID_KEY = "unit_id";
        public static final String MODULE_ID_KEY = "module_id";
        public static final String QUESTION_COUNT_KEY = "question_count";
        public static final String CORRECTNESS_RATIO_KEY = "correctness_ratio";
        public static final String TOTAL_DURATION_KEY = "total_duration";
        public static final String RESULT_DETAILS_KEY = "result_details";
        public static final String QUESTION_ID_KEY = "question_id";
        public static final String CORRECT_KEY = "correct";
        public static final String DURATION_KEY = "duration";
        public static final String START_KEY = "start";
        public static final String END_KEY = "end";
        public static final String MODULE_KEY = "module";
        public static final String RANGE_KEY = "range";
        public static final String PROGRESS_KEY = "progress";
        public static final String CONTENT_TYPE_KEY = "content_type";
        public static final String SCORE_KEY = "score";
        public static final String COURSE_KEY = "course";
        public static final String ID_KEY = "id";
        public static final String PRACTICE_ID_KEY = "practice_id";
        public static final String COIN_KEY = "coin";
        public static final String COINS_KEY = "coins";
        public static final String PERFECTION_KEY = "perfection";
        public static final String LAST_UP_COIN_KEY = "last_up_coin";
        public static final String LINK_DOWNLOAD_UNIT = "http://192.168.0.249:8000/minna1.zip";
        public static final String UNIT = "unit";
        public static final String RANKING_KEY = "ranking";
        public static final String LATEST = "latest";
        public static final String CODE = "code";
        public static final String DOWNLOAD_URL = "download_url";
        public static final String NEW_FEATURE_VN = "new_feature_vi";
        public static final String NEW_FEATURE_EN = "new_feature_en";
        public static final String NEW_FEATURE_JA = "new_feature_ja";
    }

    /**
     * Define API
     */
    public class API {
        public static final String REGISTER = "v1/auth/register";
        public static final String FACEBOOK_LOGIN = "v1/auth/facebook";
        public static final String FACEBOOK_CONNECT = "v1/auth/facebook/link";
        public static final String FACEBOOK_DISCONNECT = "v1/auth/facebook/unlink";
        public static final String GET_ACCESS_TOKEN = "v1/auth/token";
        public static final String GET_USER = "v1/me";
        public static final String GET_LIST_COURSE = "v1/courses";
        public static final String GET_LIST_TEAM = "v1/teams";
        public static final String GET_TEAM_DETAIL = "v1/teams/%d";
        public static final String GET_TEAM_LEARNED_MAP = "v1/teams/%d/study-map/%d";
        public static final String GET_TEAM_CODE = "v1/code";
        public static final String GET_TERMS_OF_SERVICE = "v1/terms-of-service";
        public static final String GET_COURSE_INFO = "v1/courses/%d";
        public static final String GET_USER_CODE = "v1/code";
        public static final String GET_RANKING_TODAY = "v1/user-test-ranking-day/%d";
        public static final String GET_RANKING_WEEK = "v1/user-test-ranking-week/%d";
        public static final String USER_POINT_TEST = "v1/user-point-test";
        public static final String GET_LIST_COURSE_OF_USER = "v1/users/%d/courses";
        public static final String UPDATE_AVATAR = "v1/avatar";
        public static final String UPDATE_PASSWORD = "v1/password";
        public static final String SEND_RESET_CODE = "v1/password/send-reset-code";
        public static final String CONFIRM_RESET_CODE = "v1/password/confirm-reset-code";
        public static final String PASSWORD_RESET = "v1/password/reset";
        public static final String UPDATE_PROFILE = "v1/profile";
        public static final String LOGOUT = "v1/auth/logout";
        public static final String LOG = "v1/log";
        public static final String LOG_PROGRESS = "v1/log/progress";
        public static final String LOG_RANKING = "v1/log/ranking";
        public static final String LOG_RANKING_AT_WEEK = "v1/log/ranking?week=true";
        public static final String LOG_DOWNLOAD = "v1/log/download";
        public static final String LOG_DOWNLOAD_RECENT = "v1/log/download/recent?date=%s";
        public static final String LOG_COIN = "v1/log/coins";
        public static final String LOG_COIN_ON_DAY = "v1/log/coins?team=%d";
        public static final String LOG_COIN_AT_WEEK = "v1/log/coins?week=true&team=%d";
        public static final String MESSAGE_IN_TEAM = "v1/messages/team/%d?page=%d";
        public static final String DEVICE_TOKEN = "v1/devices";
        public static final String CHECK_VERSION_APP = "version/android.json";

        /**
         * Lấy danh sách Lessons List.
         */
        public static final String GET_LESSONS = "v1/lessons";

        public static final String DOWNLOAD_LESSONS_LIST = "v2/download/lessons/%d";

        public static final String GET_URL_DOWNLOAD_LESSON = "v2/lessons/%d";
    }

    /**
     * Define SharedPreferencesKey Key
     */
    public class SharedPreferencesKey {
        public static final String USER_ID = "user_id";
        public static final String USER_EMAIL = "user_email";
        public static final String USER_NAME = "user_name";
        public static final String FULL_NAME = "full_name";
        public static final String HAS_PASSWORD = "has_password";
        public static final String USER_AVATAR = "user_avatar";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String EXPIRES_IN = "expires_in";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String FACEBOOK_CONNECTED = "facebook_connected";
        public static final String LAST_CONNECTION_USER_ID = "last_connection_user_id";

        public static final String STATUS_LESSONS_API = "status_lessons_from_api";
    }

    public class Database {
        public static final String REALM = ".realm";
        public static final String FIELD_ID = "id";
        public static final String FIELD_TYPE = "type";
        public static final String FIELD_IS_SEND = "is_send";
        public static final String FIELD_START = "start";
        public static final String FIELD_END = "end";
        public static final String FIELD_CREATE_AT = "create_at";
        public static final String FIELD_COURSE_ID = "course_id";
        public static final String FIELD_UNIT_ID = "unit_id";
        public static final String FIELD_MODULE_ID = "module_id";
        public static final String FIELD_NAME = "name";
        public static final String FIELD_TYPE_LESSON = "type_lesson";
        public static final String FIELD_TEST = "test";

        /**
         * Use for table lesson.
         */
        public class Lesson {

            /**
             * The private constructor.
             */
            private Lesson() {
            }

            public static final String LESSON_NAME_TABLE = "LessonDao";
            public static final String LESSON_FIELD_LESSON_ID = "lesson_id";
            public static final String LESSON_FIELD_NUMBER = "number";
            public static final String LESSON_FIELD_ID = "id";
            public static final String LESSON_FIELD_LEVEL = "level";
            public static final String LESSON_FIELD_CATEGORY = "category";
            public static final String LESSON_FIELD_TITLE_VN = "title_vi";
            public static final String LESSON_FIELD_TITLE_EN = "title_en";
            public static final String LESSON_FIELD_TITLE_JA = "title_ja";
        }

        /**
         * Use for table Knowledge..
         */
        public class Knowledge {

            /**
             * The private constructor.
             */
            private Knowledge() {
            }

            public static final String KNOWLEDGE_NAME_TABLE = "KnowledgeDao";
            public static final String KNOWLEDGE_FIELD_NUMBER = "number";
            public static final String KNOWLEDGE_FIELD_LEVEL = "level";
            public static final String KNOWLEDGE_FIELD_CATEGORY = "category";
            public static final String KNOWLEDGE_FIELD_PICTURE_FILE = "picture_file";
            public static final String KNOWLEDGE_FIELD_LESSON_ID = "lesson_id";
            public static final String KNOWLEDGE_FIELD_LESSON_NUMBER = "lesson_number";
        }

        /**
         * Use for table TimeLog.
         */
        public class TimeLog {

            /**
             * The private constructor.
             */
            private TimeLog() {
            }

            public static final String TIME_LOG_FIELD_ID = "id";
            public static final String TIME_LOG_FIELD_SHORT_DAY = "shortDay";
        }

        /**
         * Use for table Grammar.
         */
        public class Grammar {

            /**
             * The private constructor.
             */
            private Grammar() {
            }

            public static final String GRAMMAR_FIELD_LESSON_ID = "lesson_id";
            public static final String GRAMMAR_NAME_TABLE = "GrammarDao";
            public static final String GRAMMAR_FIELD_LESSON_NUMBER = "lesson_number";
        }

        /**
         * Use for table Choice.
         */
        public class Choice {

            /**
             * The private constructor.
             */
            private Choice() {
            }

            // Use for table Choice.
            public static final String CHOICE_FIELD_QUESTION_ID = "question_id";
        }

        /**
         * Use for table Question.
         */
        public class Question {

            /**
             * The private constructor.
             */
            private Question() {
            }

            public static final String QUESTION_NAME_TABLE = "QuestionDao";
            public static final String QUESTION_FIELD_ID = "id";
            public static final String QUESTION_FIELD_TYPE = "type";
            public static final String QUESTION_FIELD_CATEGORY = "category";
            public static final String QUESTION_FIELD_LESSON_ID = "lesson_id";
            public static final String QUESTION_FIELD_LESSON_NUMBER = "lesson_number";
        }

        /**
         * Use for table Ranking.
         */
        public class Ranking {

            /**
             * The private constructor.
             */
            private Ranking() {
            }

            public static final String RANKING_NAME_TABLE = "Ranking";
            public static final String RANKING_FIELD_LESSON_NUMBER = "lesson_number";
            public static final String RANKING_LEVEL = "level";
            public static final String RANKING_CATEGORY = "category";
            public static final String RANKING_LESSON_TYPE = "lesson_type";
            public static final String RANKING_QUESTION_TYPE = "question_type";
            public static final String RANKING_ARMORIAL = "armorial";
            public static final String RANKING_TIME = "time";
        }
    }

    /**
     * Define API Request Params Key
     */
    public class Request {
        public static final String HEADER_AUTHORIZATION = "Authorization";
        public static final String HEADER_ACCEPT = "Accept";
        public static final String HEADER_BEARER = "Bearer ";
        public static final String HEADER_APP_CODE = "AppCode";
        public static final String HEADER_APP_VERSION = "AppVersion";

        /**
         * Bearer constant: "Bearer".
         */
        public static final String HEADER_BEARER2 = "Bearer";

        public static final String PARAM_FULL_NAME = "full_name";
        public static final String PARAM_USER_NAME = "username";
        public static final String PARAM_EMAIL = "email";
        public static final String PARAM_RESET_CODE = "reset_code";
        public static final String PARAM_PASSWORD = "password";
        public static final String PARAM_CLIENT_ID = "client_id";
        public static final String PARAM_CLIENT_SECRET = "client_secret";
        public static final String PARAM_CLIENT_TOKEN = "ClientToken";
        public static final String PARAM_GRANT_TYPE = "grant_type";
        public static final String PARAM_FACEBOOK_ACCESS_TOKEN = "fb_access_token";
        public static final String PARAM_CURRENT_PASSWORD = "current_password";
        public static final String PARAM_NEW_PASSWORD = "new_password";
        public static final String PARAM_AVATAR = "avatar";
        public static final String PARAM_REFRESH_TOKEN = "refresh_token";
        public static final String PARAM_TYPE = "type";
        public static final String PARAM_DATA = "data";
        public static final String PARAM_DEVICE_TOKEN = "device_token";
        public static final String PARAM_OS = "os";
        public static final String PARAM_LESSON_ID = "lesson_id";
        public static final String PARAM_POINT = "point";
        public static final String PARAM_LANGUAGE_CODE = "language_code";
    }

    /**
     * Define API Response Key
     */
    public class Response {
        public static final String DATA = "data";
        public static final String TITLE = "title";
        public static final String ID = "id";
        public static final String EMAIL = "email";
        public static final String USER_NAME = "username";
        public static final String NAME = "name";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String EXPIRES_IN = "expires_in";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String ATTRIBUTES = "attributes";
        public static final String ERRORS = "errors";
        public static final String SUCCESS = "is_success";
        public static final String FACEBOOK_CONNECTED = "facebook_connected";
        public static final String URL = "url";
        public static final String HAS_PASSWORD = "has_password";
        public static final String AVATAR_URL = "avatar_url";
        public static final String CODE = "code";
        public static final String USER_COUNT = "user_count";
        public static final String USERS = "users";
        public static final String MANAGERS = "managers";
        public static final String MESSAGES = "messages";
        public static final String LAST_PAGE = "lastPage";
        public static final String CURRENT_PAGE = "currentPage";
    }

    public class Constants {
        public static final int REQ_REFRESH_TOKEN = 100;
        public static final int REQ_SEND_LOG = 101;
        public static final String TYPE_USED_APP = "online";
        public static final String TYPE_IMPROVE_KNOWLEDGE = "knowledge";
        public static final String TYPE_PRACTICE = "practice";
        public static final String TYPE_TEST = "test";
        public static final String TYPE_UNIT = "unit";
        public static final String TYPE_UNIT_CONTENT = "unit-content";
        public static final String TYPE_PRACTICE_RANKING = "practice-ranking";
        public static final String GRANT_TYPE = "password";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String ACTION_REFRESH_TOKEN = "com.honkidenihongo.action.REFRESH_TOKEN";
        public static final String ACTION_SEND_LOG = "com.honkidenihongo.action.SEND_LOG";
        public static final String RESULT_GOOD = "good";
        public static final String RESULT_BAD = "bad";
        public static final String FLASHCARD = "Flashcard";
        public static final String ANDROID = "android";
        public static final String VALUE_ACCEPT = "application/json;charset=utf-8";
    }

    public class LogReceiverConstant {
        public static final String ACTION_START_LOG = "3l.time.log.start";
        public static final String ACTION_END_LOG = "3l.time.log.end";
    }

    public static class AuthType {
        private AuthType() {
        }

        public static final int SYSTEM_SERVER = 0;
        public static final int FACEBOOK = 1;
    }

    /**
     * Class định nghĩa độ trong suốt của view đồ họa (ImageView...).
     */
    public static class Graphic {

        /**
         * The private constructor.
         */
        private Graphic() {
        }

        public static final float LIMPIDITY = 1.0f;
        public static final float BLEAR = 0.5f;
        public static final float BLEAR_NATIONAL_FLAG = 0.3f;
    }

    /**
     * Class định nghĩa các font dùng cho textView.
     */
    public static class Fonts {

        /**
         * The private constructor.
         */
        private Fonts() {
        }

        public static final String FONT_DEFAULT = "SERIF";
        public static final String FONT_KLEE = "klee";
        public static final String PATH_FONT_KLEE = "fonts/klee.ttc";
        public static final String PATH_FONT_ROBOTO = "fonts/roboto_regular.ttf";
    }

    /**
     * Class check valid for username, email...vv.
     */
    public static class Valid {

        /**
         * The private constructor.
         */
        private Valid() {
        }

        public static final String EMAIL = "\\A([a-z0-9]+)(([\\.\\-]?[a-z0-9_]+)*)@([a-z0-9]+)(([\\.\\-]?[a-z0-9_]+)*)((\\.([a-z0-9_]+))+)\\z";
        public static final String USER_NAME = "\\A[a-z][a-z0-9\\.]{2,17}[a-z0-9]\\z";
    }
}
