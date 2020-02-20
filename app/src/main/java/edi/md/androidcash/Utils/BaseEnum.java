package edi.md.androidcash.Utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Tony on 2017/12/3.
 */

public class BaseEnum {
    public static final int FISCAL_SERVICE = 1, FISCAL_DEVICE = 2 , NONE_SELECTED_FISCAL_MODE = 0;
    public static final int DATECS_USB_VID = 65520, FTDI_USB_VID = 1027;
    public static final int BILL_CLOSED = 1, BILL_OPEN = 0, BILL_DELETED = 2;


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

}
