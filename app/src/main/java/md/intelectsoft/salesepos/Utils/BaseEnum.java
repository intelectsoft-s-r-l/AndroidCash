package md.intelectsoft.salesepos.Utils;

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
    public static final int Dialog_CheckPrice = 48, Dialog_AddItem = 58;
    public static final int Activity_History = 101 , Activity_Settings = 102, Activity_Main = 103, Activity_Reports = 104, Activity_FinRep = 105, Activity_Tickets = 106, Activity_Shifts = 107;
    public static final int History_CreateBill = 11, History_AddedToBill = 12, History_DeletedBill = 13, History_ClosedBill = 14, History_DeletedFromBill = 15, History_AddedClientToBill = 16,
            History_DeletedClientFromBill = 17, History_OpenShift = 18, History_ClosedShift = 19, History_Printed_X = 20, History_Printed_Z = 21, History_AddedDiscount = 22, History_DeletedDiscount = 23,
            History_PaymentBill = 24, History_SynchronizationStarted = 25, History_SynchronizationFinish = 26, History_DeferredBill = 27, History_InsertMoneyToDraw = 28, History_WithdrawMoneyFromDraw = 29,
            History_CollectionMoney = 30, History_UserLogIn = 31, History_UserLogOut = 32, History_RecreatBill = 33, History_ChangeItemCount = 34;

    public static final int FiscalPrint_Master = 0, FiscalPrint_Slave = 1;

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
    @IntDef({Dialog_CheckPrice, Dialog_AddItem})
    @Retention(RetentionPolicy.SOURCE)
    public @interface dialogType {
    }
    @IntDef({Activity_History, Activity_Settings, Activity_Main, Activity_Reports, Activity_FinRep, Activity_Tickets, Activity_Shifts})
    @Retention(RetentionPolicy.SOURCE)
    public @interface activityType {
    }
    @IntDef({History_CreateBill, History_AddedToBill, History_DeletedBill, History_ClosedBill, History_DeletedFromBill, History_AddedClientToBill, History_DeletedClientFromBill, History_OpenShift, History_ClosedShift,
            History_Printed_X, History_Printed_Z, History_AddedDiscount, History_DeletedDiscount, History_PaymentBill, History_SynchronizationStarted, History_SynchronizationFinish, History_DeferredBill,
            History_InsertMoneyToDraw, History_WithdrawMoneyFromDraw, History_CollectionMoney, History_UserLogIn, History_UserLogOut, History_RecreatBill, History_ChangeItemCount})
    @Retention(RetentionPolicy.SOURCE)
    public @interface historyType {
    }

    @IntDef({FiscalPrint_Master,FiscalPrint_Slave})
    @Retention(RetentionPolicy.SOURCE)
    public @interface fiscalTypePrint {
    }
}
