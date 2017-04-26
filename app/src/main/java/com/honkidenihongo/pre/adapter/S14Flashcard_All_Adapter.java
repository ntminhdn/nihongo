package com.honkidenihongo.pre.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.drawable.PictureDrawable;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.caverock.androidsvg.SVG;
import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.FontsConfig;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.gui.flashcard.S14Flashcard_Activity;
import com.honkidenihongo.pre.gui.flashcard.S14Flashcard_All_Fragment;
import com.honkidenihongo.pre.gui.widget.svg.SvgDecoder;
import com.honkidenihongo.pre.gui.widget.svg.SvgDrawableTranscoder;
import com.honkidenihongo.pre.gui.widget.svg.SvgSoftwareLayerSetter;
import com.honkidenihongo.pre.model.constant.Category;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;


/**
 * Class Adapter for class {@link S14Flashcard_All_Fragment}.
 *
 * @author binh.dt.
 * @since 08-Nov-2016.
 */
public class S14Flashcard_All_Adapter extends BaseAdapter {
    private static final String LOG_TAG = S14Flashcard_All_Adapter.class.getName();
    private List<KnowledgeDao> mKnowledgeDetailDaoList;
    private S14Flashcard_All_Fragment mS14FlashcardAllFragment;
    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> mRequestBuilder;
    private Context mContext;

    /**
     * Constructor of class.
     *
     * @param context                 Value of screen current.
     * @param knowledgeDetailDaoList  List knowledge truyền vào adapter.
     * @param s14FlashcardAllFragment Fragment current.
     */
    public S14Flashcard_All_Adapter(Context context, List<KnowledgeDao> knowledgeDetailDaoList, S14Flashcard_All_Fragment s14FlashcardAllFragment) {
        mContext = context;
        mKnowledgeDetailDaoList = knowledgeDetailDaoList;
        mS14FlashcardAllFragment = s14FlashcardAllFragment;

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
        if (mKnowledgeDetailDaoList != null && mKnowledgeDetailDaoList.size() > 0 && i < mKnowledgeDetailDaoList.size()) {
            return mKnowledgeDetailDaoList.get(i);
        }

        return null;
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
            convertView = inflater.inflate(R.layout.s14_flashcard_all_item_list, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTvKana = (AppCompatTextView) convertView.findViewById(R.id.mTvKana);
        holder.mTvMeaning = (AppCompatTextView) convertView.findViewById(R.id.mTvMeaning);
        holder.mTvLanguage = (AppCompatTextView) convertView.findViewById(R.id.mTvLanguage);
        holder.mImgSound = (AppCompatImageButton) convertView.findViewById(R.id.mImgSound);
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

        holder.mTvKana.setText(mKnowledgeDetailDaoList.get(position).subject_kana);

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
        if (mS14FlashcardAllFragment.getDataLesson2() != null
                && mS14FlashcardAllFragment.getDataLesson2().getCategory() == Category.PRE_HIRAGANA
                || mS14FlashcardAllFragment.getDataLesson2().getCategory() == Category.PRE_KATAKANA) {
            holder.mTvLanguage.setText(mContext.getString(R.string.common_app__romaji));
        }

        // Kiểm tra if category của lesson hiện tại là Hiragana or Katakana thì hiện thị nội dung romaji.
        if (mS14FlashcardAllFragment.getDataLesson2() != null
                && mS14FlashcardAllFragment.getDataLesson2().getCategory() == Category.PRE_HIRAGANA
                || mS14FlashcardAllFragment.getDataLesson2().getCategory() == Category.PRE_KATAKANA) {
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
                mS14FlashcardAllFragment.onImageSoundAdapterClick(position);
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

//    /**
//     * Todo method using later.
//     * Load photo from internet.
//     *
//     * @param requestBuilder     Value object GenericRequestBuilder.
//     * @param appCompatImageView Photo display inside imageView.
//     */
//    private void loadFromNet(GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder, AppCompatImageView appCompatImageView) {
//        Uri uri = Uri.parse("http://www.clker.com/cliparts/u/Z/2/b/a/6/android-toy-h.svg");
//
//        requestBuilder.diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                // SVG cannot be serialized so it's not worth to cache it.
//                .load(uri)
//                .into(appCompatImageView);
//    }

    /**
     * Load photo from local.
     *
     * @param requestBuilder     Value object GenericRequestBuilder.
     * @param appCompatImageView Photo display inside imageView.
     */
    private void loadFromRes(int position, GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder, AppCompatImageView appCompatImageView) {
        File fileSvg = mS14FlashcardAllFragment.getFileSvgOfLesson(position);

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
        private AppCompatTextView mTvMeaning;
        private AppCompatImageButton mImgSound;
        private AppCompatTextView mTvLanguage;
        private AppCompatImageView mImgDescription;
    }
}
