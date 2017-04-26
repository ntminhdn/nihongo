package com.honkidenihongo.pre.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;
import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.FontsConfig;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.gui.flashcard.S14Flashcard_Activity;
import com.honkidenihongo.pre.gui.flashcard.S14Flashcard_Note_Fragment;
import com.honkidenihongo.pre.gui.widget.svg.SvgDecoder;
import com.honkidenihongo.pre.gui.widget.svg.SvgDrawableTranscoder;
import com.honkidenihongo.pre.gui.widget.svg.SvgSoftwareLayerSetter;
import com.honkidenihongo.pre.model.constant.Category;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Class Adapter for class {@link S14Flashcard_Note_Fragment}.
 *
 * @author binh.dt.
 * @since 08-Nov-2016.
 */
public class S14Flashcard_Note_Adapter extends BaseAdapter {
    private static final String LOG_TAG = S14Flashcard_Note_Adapter.class.getName();
    private Context mContext;
    private List<KnowledgeDao> mKnowledgeDetailDaoList;
    private S14Flashcard_Note_Fragment mS14FlashcardNoteFragment;
    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> mRequestBuilder;

    /**
     * Constructor of Class.
     *
     * @param context                    Value screen current.
     * @param knowledgeDetailDaoList     List knowledge truyền vào adapter.
     * @param s14Flashcard_note_fragment Fragment current.
     */
    public S14Flashcard_Note_Adapter(Context context, List<KnowledgeDao> knowledgeDetailDaoList, S14Flashcard_Note_Fragment s14Flashcard_note_fragment) {
        mContext = context;
        mKnowledgeDetailDaoList = knowledgeDetailDaoList;
        mS14FlashcardNoteFragment = s14Flashcard_note_fragment;

        mRequestBuilder= Glide.with(mContext)
                .using(Glide.buildStreamModelLoader(Uri.class, mContext), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<>(new SvgDecoder()))
                .decoder(new SvgDecoder())
                .placeholder(null)
                .error(null)
                .animate(android.R.anim.fade_in)
                .listener(new SvgSoftwareLayerSetter<Uri>());
    }

    @Override
    public int getCount() {
        return mKnowledgeDetailDaoList == null ? 0 : mKnowledgeDetailDaoList.size();
    }

    @Override
    public Object getItem(int i) {
        return mKnowledgeDetailDaoList == null ? null : mKnowledgeDetailDaoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.s14_flashcard_note_item_list, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTvKana = (AppCompatTextView) convertView.findViewById(R.id.mTvKana);
        holder.mTvLanguage = (AppCompatTextView) convertView.findViewById(R.id.mTvLanguage);
        holder.mImgSound = (AppCompatImageButton) convertView.findViewById(R.id.mImgSound);
        holder.mTvMeaning = (AppCompatTextView) convertView.findViewById(R.id.mTvMeaning);
        holder.mImgDescription = (AppCompatImageView) convertView.findViewById(R.id.mImgDescription);

        // Needed because of image accelaration in some devices such as samsung.
        holder.mImgDescription.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        clearCache(position, mRequestBuilder, holder.mImgDescription);

        // Todo sẽ remove dòng lệnh phía dưới đi trong version sau bởi vì hiện tại app chỉ dùng cho người việt.
        holder.mTvLanguage.setVisibility(View.GONE);

        Typeface fontDisplay = FontsConfig.getInstance(mContext).getFont(FontsConfig.AppFont.KLEE);

        if (fontDisplay != null) {
            holder.mTvKana.setTypeface(fontDisplay);
        }

        holder.mTvKana.setText(String.valueOf(mKnowledgeDetailDaoList.get(position).subject_kana));

        // Todo sẽ open các dòng code phía dưới ở version sau bởi vì hiện tại version này chỉ dùng cho người việt.
        // Kiểm tra ngôn ngữ hiện thị lên text mTvMeaning.
//        String meaning = "";
//
//        if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH)) {
//            meaning = mKnowledgeDetailDaoList.get(position).meaning_en;
//        } else {
//            meaning = mKnowledgeDetailDaoList.get(position).meaning_vi;
//        }

        holder.mTvMeaning.setText(mKnowledgeDetailDaoList.get(position).meaning_vi);

        // Kiểm tra if category của lesson hiện tại là Hiragana or Katakana thì hiện thị nội dung là romaji cho text language.
        if (mS14FlashcardNoteFragment.getDataLesson2() != null
                && mS14FlashcardNoteFragment.getDataLesson2().getCategory() == Category.PRE_HIRAGANA
                || mS14FlashcardNoteFragment.getDataLesson2().getCategory() == Category.PRE_KATAKANA) {
            holder.mTvLanguage.setText(mContext.getString(R.string.common_app__romaji));
        }

        // Kiểm tra if category của lesson hiện tại là Hiragana or Katakana thì hiện thị nội dung là romaji.
        if (mS14FlashcardNoteFragment.getDataLesson2() != null
                && mS14FlashcardNoteFragment.getDataLesson2().getCategory() == Category.PRE_HIRAGANA
                || mS14FlashcardNoteFragment.getDataLesson2().getCategory() == Category.PRE_KATAKANA) {
            holder.mTvMeaning.setText(mKnowledgeDetailDaoList.get(position).subject_romaji);
        }

        // Kiểm tra làm mờ image âm thanh.
        if (mContext instanceof S14Flashcard_Activity) {
            File fileAudio = ((S14Flashcard_Activity) mContext).getFileAudio(mKnowledgeDetailDaoList.get(position).category, mKnowledgeDetailDaoList.get(position).voice_file);

            // Kiểm tra file audio có tồn tại hay không if tồn tại thì show , không tồn tại thì làm mờ nó đi và không cho click.
            if (fileAudio != null && fileAudio.exists()) {
                holder.mImgSound.setAlpha(Definition.Graphic.LIMPIDITY);
                holder.mImgSound.setEnabled(true);
            } else {
                holder.mImgSound.setAlpha(Definition.Graphic.BLEAR);
                holder.mImgSound.setEnabled(false);
            }
        }

        holder.mImgSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mS14FlashcardNoteFragment.onImageSoundAdapterClick(position);
            }
        });

        return convertView;
    }

    /**
     * Method using clear memory cash.
     *
     * @param requestBuilder     Value object GenericRequestBuilder.
     * @param appCompatImageView Photo display inside imageView.
     */
    private void clearCache(int position, GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder, AppCompatImageView appCompatImageView) {
        Glide.clear(appCompatImageView);
        Glide.get(mContext).clearMemory();
        File cacheDir = Glide.getPhotoCacheDir(mContext);

        if (cacheDir.isDirectory()) {
            for (File child : cacheDir.listFiles()) {
                if (!child.delete()) {
                    Log.w(LOG_TAG, "cannot delete: " + child);
                }
            }
        }

        loadFromRes(position, requestBuilder, appCompatImageView);
    }

    /**
     * Load photo from local.
     *
     * @param requestBuilder     Value object GenericRequestBuilder.
     * @param appCompatImageView Photo display inside imageView.
     */
    private void loadFromRes(int position, GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder, AppCompatImageView appCompatImageView) {
        File fileSvg = mS14FlashcardNoteFragment.getFileSvgOfLesson(position);

        if (fileSvg != null) {
            appCompatImageView.setVisibility(View.VISIBLE);

            requestBuilder.diskCacheStrategy(DiskCacheStrategy.NONE)
                    // SVG cannot be serialized so it's not worth to cache it.
                    // and the getResources() should be fast enough when acquiring the InputStream.
                    .load(Uri.fromFile(fileSvg))
                    .into(appCompatImageView);
        } else {
            appCompatImageView.setVisibility(View.GONE);
        }
    }

    /**
     * Class ViewHolder.
     */
    private static class ViewHolder {
        private AppCompatTextView mTvKana;
        private AppCompatImageButton mImgSound;
        private AppCompatTextView mTvLanguage;
        private AppCompatTextView mTvMeaning;
        private AppCompatImageView mImgDescription;
    }
}
