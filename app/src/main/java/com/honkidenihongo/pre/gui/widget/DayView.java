package com.honkidenihongo.pre.gui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honkidenihongo.pre.R;

/**
 * Custom DayView..
 * Modify.
 *
 * @author binh.dt.
 * @since 20-Dec-2016.
 */
public class DayView extends FrameLayout {

    public enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    // For view.
    private TextView mTvDayName;
    private PercentView mViewPercent;
    private Context mContext;

    public static DayView newInstance(Context context, Day day, double percent) {
        DayView dayView = new DayView(context);
        dayView.setDay(day);
        dayView.setPercent(percent);

        return dayView;
    }

    public DayView(Context context) {
        super(context);
        init(context);
    }

    public DayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("InflateParams")
    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.s03_dash_board_view_day, null, false);

        mTvDayName = (TextView) view.findViewById(R.id.mTvDayName);
        mViewPercent = (PercentView) view.findViewById(R.id.mViewPercent);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        addView(view, params);
    }

    /**
     * Method dùng để set ngày for view.
     *
     * @param day Value.
     */
    private void setDay(Day day) {
        String dayName;

        switch (day) {
            case MONDAY:
                dayName = mContext.getResources().getString(R.string.s03_dashboard__day_monday);

                break;
            case TUESDAY:
                dayName = mContext.getResources().getString(R.string.s03_dashboard__day_tuesday);

                break;
            case WEDNESDAY:
                dayName = mContext.getResources().getString(R.string.s03_dashboard__day_wednesday);

                break;
            case THURSDAY:
                dayName = mContext.getResources().getString(R.string.s03_dashboard__day_thursday);

                break;
            case FRIDAY:
                dayName = mContext.getResources().getString(R.string.s03_dashboard__day_friday);

                break;
            case SATURDAY:
                dayName = mContext.getResources().getString(R.string.s03_dashboard__day_saturday);

                break;
            case SUNDAY:
                dayName = mContext.getResources().getString(R.string.s03_dashboard__day_sunday);

                break;
            default:
                dayName = "";

                break;
        }

        mTvDayName.setText(dayName);
    }

    /**
     * Value phần trăm đã học ở ngày được chọn.
     */
    private void setPercent(double percent) {
        mViewPercent.setPercentage(percent);
    }
}
