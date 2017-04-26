package com.honkidenihongo.pre.service.clickhomedevice;

/**
 * * Class dùng để lắng nghe sự kiện click nút home và percent của device.
 * reference: http://stackoverflow.com/questions/31340715/android-associate-a-method-to-home-button-of-smartphone/31340960
 *
 * @author Binh.dt.
 */
public interface OnHomePressedListener {
    void onSystemMenuPressed();

    void onHomePressed();

    void offScreenPressed();
}
