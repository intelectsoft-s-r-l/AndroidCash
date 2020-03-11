package edi.md.androidcash.Utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Tony on 2017/12/3.
 */

public class BaseEnum {
    public static final int FISCAL_SERVICE = 2, FISCAL_DEVICE = 1 , NONE_SELECTED_FISCAL_MODE = 0;
    public static final int DATECS_USB_VID = 65520, FTDI_USB_VID = 1027;
    public static final int BILL_CLOSED = 1, BILL_OPEN = 0, BILL_DELETED = 2;
    public static final int Pay_Cash = 1, Pay_CreditCard = 2, Pay_Coupon = 3, Pay_BankTransfer = 4, Pay_ClientAccount = 5, Pay_TMH = 7, Pay_TME = 8;

    @IntDef({FISCAL_SERVICE, FISCAL_DEVICE, NONE_SELECTED_FISCAL_MODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface fiscalCommands {
    }
    @IntDef({DATECS_USB_VID, FTDI_USB_VID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface datecsVID {
    }

    @IntDef({BILL_CLOSED, BILL_OPEN, BILL_DELETED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface billState {
    }
    @IntDef({Pay_Cash, Pay_CreditCard, Pay_Coupon, Pay_BankTransfer, Pay_ClientAccount, Pay_TMH, Pay_TME})
    @Retention(RetentionPolicy.SOURCE)
    public @interface payIndex {
    }
}
