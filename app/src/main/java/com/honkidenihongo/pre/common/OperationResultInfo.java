package com.honkidenihongo.pre.common;

/**
 * Class thể hiện đối tượng thông tin Kết Quả thao tác chung.
 *
 * @author long.tt.
 * @since 01-Dec-2016.
 */
public class OperationResultInfo {
    /**
     * Kết quả thành công hay không thành công.
     */
    public boolean isSuccess;

    /**
     * Error code, nếu không có lỗi thì errorCode = 0.
     */
    public int errorCode;

//    public int errorMessageJa;
//    public int errorMessageVi;
//    public int errorMessageEn;

}
