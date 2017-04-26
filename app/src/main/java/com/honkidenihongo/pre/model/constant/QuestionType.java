package com.honkidenihongo.pre.model.constant;

/**
 * Hằng số thể hiện các kiểu QuestionType như: Text or Voice, Japanese or Native-LanguageCode.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public final class QuestionType {
    /**
     * The private constructor to prevent creating new object.
     */
    private QuestionType() {
    }

    /**
     * The Question is in Japanese text, the Choice is in Native-Language (English, Vietnamese) text.
     */
    public static final int TEXT_JA_NLANG = 31;

    /**
     * The Question is in Native-Language (English, Vietnamese) text, the Choice is in Japanese text.
     */
    public static final int TEXT_NLANG_JA = 33;

    /**
     * The Question is in Japanese text, the Choice is in Japanese text.
     */
    public static final int TEXT_JA_JA = 32;

    /**
     * The Question is in Kana(Hiragana or Katakana) text, the Choice is Romaji text.
     */
    public static final int TEXT_KANA_ROMAJI = 11;

    /**
     * The Question is in Romaji text, the Choice is in Kana(Hiragana or Katakana) text.
     */
    public static final int TEXT_ROMAJI_KANA = 13;

    /**
     * The Question is in Japanese voice, the Choice is in Native-Language (English, Vietnamese) text.
     */
    public static final int VOICE_JA_NLANG = 41;

    /**
     * The Question is in Native-LanguageCode (English, Vietnamese) voice, the Choice is in Japanese text.
     */
    public static final int VOICE_NLANG_JA = 43;

    /**
     * The Question is in Japanese voice, the Choice is in Japanese text.
     */
    public static final int VOICE_JA_JA = 42;

    /**
     * The Question is in Kana(Hiragana or Katakana) voice, the Choice is in Romaji text.
     */
    public static final int VOICE_KANA_ROMAJI = 21;

    /**
     * The Question is in Kana(Hiragana or Katakana) voice, the Choice is in Kana text.
     */
    public static final int VOICE_KANA_KANA = 22;
}
