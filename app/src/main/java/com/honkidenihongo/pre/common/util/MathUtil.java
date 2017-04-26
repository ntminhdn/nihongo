package com.honkidenihongo.pre.common.util;

import java.math.BigDecimal;

/**
 * Class MathUtil support for data.
 *
 * @author binh.dt.
 * @since 05-Jan-2017.
 */
public class MathUtil {

    /**
     * The private constructor.
     */
    private MathUtil() {
    }

    /**
     * Method dùng để tính thời gian user khi thực hiện test có làm tròn.
     *
     * @param timeCompleted Value time complete.
     * @param decimalPlace  Value decimalPlace.
     * @return Value double.
     */
    public static double round(double timeCompleted, int decimalPlace) {
        if (decimalPlace < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(timeCompleted);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);

        return bd.doubleValue();
    }
}
