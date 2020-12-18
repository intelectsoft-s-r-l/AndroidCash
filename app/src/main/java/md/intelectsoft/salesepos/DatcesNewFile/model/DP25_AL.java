package md.intelectsoft.salesepos.DatcesNewFile.model;

import com.datecs.fiscalprinter.SDK.AbstractTransportProtocol;
import com.datecs.fiscalprinter.SDK.FiscalDeviceV2;
import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.FiscalResponse;
import com.datecs.fiscalprinter.SDK.TransportProtocolV2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DP25_AL extends FiscalDeviceV2 {
    private final String[] printColumnsSupported;
    private final Integer MAX_PLU;
    private final int maxNonFiscalText;
    private final int maxTextHeaderFooter;
    private final boolean capHasCutter;
    private boolean capFontSupprted;
    private boolean capBarcodeSupported;
    private final String defaultOpPass;

    public String getDefaultOpPass() {
        return "1";
    }

    public String detectConnectedModel() {
        return null;
    }

    public DP25_AL(InputStream in, OutputStream out) throws IOException, FiscalException, IllegalArgumentException {
        this(new TransportProtocolV2(in, out, 1250));
    }

    public DP25_AL(AbstractTransportProtocol protocol) throws IllegalArgumentException {
        super(protocol);
        this.printColumnsSupported = new String[]{"42"};
        this.MAX_PLU = 100000;
        this.maxNonFiscalText = 42;
        this.maxTextHeaderFooter = 42;
        this.capHasCutter = false;
        this.capFontSupprted = false;
        this.capBarcodeSupported = true;
        this.defaultOpPass = "1";
    }

    public FiscalResponse command33ariant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(33, InputString);
        split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        return R;
    }

    public FiscalResponse command35Variant0Version0(String text) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + text + "\t";
        String OutpS = this.customCommand(35, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command38Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(38, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("slipNumber", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command39Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(39, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("slipNumber", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command42Variant0Version0(String text) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + text + "\t";
        String OutpS = this.customCommand(42, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command42Variant1Version0(String text, String bold, String italic, String doubleH, String underline, String alignment) throws IOException, FiscalException {
        String InputString = "";
        InputString = InputString + text + "\t" + bold + "\t" + italic + "\t" + doubleH + "\t" + underline + "\t" + alignment + "\t";
        String OutpS = this.customCommand(42, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command44Variant0Version0(String lines) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + lines + "\t";
        String OutpS = this.customCommand(44, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command45Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(45, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command46Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(46, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command47Variant0Version0(String text) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + text + "\t";
        String OutpS = this.customCommand(47, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command48Variant0Version0(String opCode, String opPwd, String tillNmb, String invoice, String invoiceNumber) throws IOException, IllegalArgumentException, FiscalException {
        String InputString = "";
        InputString = InputString + opCode + "\t";
        InputString = InputString + opPwd + "\t";
        InputString = InputString + tillNmb + "\t";
        InputString = InputString + invoice + "\t";
        InputString = InputString + invoiceNumber + "\t";
        String OutpS = this.customCommand(48, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("slipNumber", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command49Variant0Version0(String pluName, String taxCd, String price, String quantity, String discountType, String discountValue, String department, String unit) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + pluName + "\t";
        InputString = InputString + taxCd + "\t";
        InputString = InputString + price + "\t";
        InputString = InputString + quantity + "\t";
        InputString = InputString + discountType + "\t";
        InputString = InputString + discountValue + "\t";
        InputString = InputString + department + "\t";
        InputString = InputString + unit + "\t";
        String OutpS = this.customCommand(49, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("slipNumber", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command50Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(50, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(11);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("nZreport", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("taxA", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("taxB", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("taxC", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("taxD", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("taxE", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("taxF", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("taxG", myOutputList[8]);
        }

        if (myOutputList.length > 9) {
            R.put("taxH", myOutputList[9]);
        }

        if (myOutputList.length > 10) {
            R.put("entDate", myOutputList[10]);
        }

        return R;
    }

    public FiscalResponse command51Variant0Version0(String print, String display, String discountType, String discountValue) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + print + "\t" + display + "\t";
        InputString = InputString + discountType + "\t";
        InputString = InputString + discountValue + "\t";
        String OutpS = this.customCommand(51, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(11);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("slipNumber", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("subtotal", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("taxA", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("taxB", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("taxC", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("taxD", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("taxE", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("taxF", myOutputList[8]);
        }

        if (myOutputList.length > 9) {
            R.put("taxG", myOutputList[9]);
        }

        if (myOutputList.length > 10) {
            R.put("taxH", myOutputList[10]);
        }

        return R;
    }

    public FiscalResponse command52Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(52, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("statusBytes", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command53Variant0Version0(String paidMode, String amount) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + paidMode + "\t";
        InputString = InputString + amount + "\t";
        String OutpS = this.customCommand(53, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(3);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("status", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("amount", myOutputList[2]);
        }

        return R;
    }

    public FiscalResponse command53Variant1Version0(String amount, String type) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "2\t";
        InputString = InputString + amount + "\t";
        InputString = InputString + type + "\t";
        String OutpS = this.customCommand(53, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(3);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("sum", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("cardNum", myOutputList[2]);
        }

        return R;
    }

    public FiscalResponse command53Variant2Version0(String amount, String change) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "6\t";
        InputString = InputString + amount + "\t";
        InputString = InputString + change + "\t";
        String OutpS = this.customCommand(53, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(3);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("status", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("amount", myOutputList[2]);
        }

        return R;
    }

    public FiscalResponse command54Variant0Version0(String text) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + text + "\t";
        String OutpS = this.customCommand(54, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command54Variant1Version0(String text, String bold, String italic, String doubleH, String underline, String alignment) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + text + "\t" + bold + "\t" + italic + "\t" + doubleH + "\t" + underline + "\t" + alignment + "\t";
        String OutpS = this.customCommand(54, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command56Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(56, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("slipNumber", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command58Variant0Version0(String pluCode, String quanity, String price, String discountType, String discountValue) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + pluCode + "\t";
        InputString = InputString + quanity + "\t";
        InputString = InputString + price + "\t";
        InputString = InputString + discountType + "\t";
        InputString = InputString + discountValue + "\t";
        String OutpS = this.customCommand(58, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("slipNumber", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command60Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(60, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command61Variant0Version0(String dateTime) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + dateTime + "\t";
        String OutpS = this.customCommand(61, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command62Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(62, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("dateTime", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command63Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(62, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("dateTime", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command64Variant0Version0(String type) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + type + "\t";
        String OutpS = this.customCommand(64, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(11);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("nRep", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("sumA", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("sumB", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("sumC", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("sumD", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("sumE", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("sumF", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("sumG", myOutputList[8]);
        }

        if (myOutputList.length > 9) {
            R.put("sumH", myOutputList[9]);
        }

        if (myOutputList.length > 10) {
            R.put("date", myOutputList[10]);
        }

        return R;
    }

    public FiscalResponse command65Variant0Version0(String type) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + type + "\t";
        String OutpS = this.customCommand(65, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(10);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("nRep", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("sumA", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("sumB", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("sumC", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("sumD", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("sumE", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("sumF", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("sumG", myOutputList[8]);
        }

        if (myOutputList.length > 9) {
            R.put("sumH", myOutputList[9]);
        }

        return R;
    }

    public FiscalResponse command68Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(68, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("reportsLeft", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command69Variant0Version0(String reportType) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + reportType + "\t";
        String OutpS = this.customCommand(69, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(18);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("nRep", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("totA", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("totB", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("totC", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("totD", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("totE", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("totF", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("totG", myOutputList[8]);
        }

        if (myOutputList.length > 9) {
            R.put("totH", myOutputList[9]);
        }

        if (myOutputList.length > 10) {
            R.put("storA", myOutputList[10]);
        }

        if (myOutputList.length > 11) {
            R.put("storB", myOutputList[11]);
        }

        if (myOutputList.length > 12) {
            R.put("storC", myOutputList[12]);
        }

        if (myOutputList.length > 13) {
            R.put("storD", myOutputList[13]);
        }

        if (myOutputList.length > 14) {
            R.put("storE", myOutputList[14]);
        }

        if (myOutputList.length > 15) {
            R.put("storF", myOutputList[15]);
        }

        if (myOutputList.length > 16) {
            R.put("storG", myOutputList[16]);
        }

        if (myOutputList.length > 17) {
            R.put("storH", myOutputList[17]);
        }

        return R;
    }

    public FiscalResponse command70Variant0Version0(String type, String amount) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + type + "\t";
        InputString = InputString + amount + "\t";
        String OutpS = this.customCommand(70, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(4);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("cashSum", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("cashIn", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("cashOut", myOutputList[3]);
        }

        return R;
    }

    public FiscalResponse command71Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "2";
        String OutpS = this.customCommand(71, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(18);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("LastDate", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("NextDate", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("Zrep", myOutputList[3]);
        }

        if (myOutputList.length > 5) {
            R.put("ErrZnum", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("ErrCnt", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("ErrNum", myOutputList[7]);
        }

        return R;
    }

    public FiscalResponse command71Variant1Version0(String infoType) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + infoType + "\t";
        String OutpS = this.customCommand(71, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command72Variant0Version0(String ActionID, String SerialNumber, String FMnumber, String TAXnumber, String TAXlabel, String TechID, String NexusCode, String HeaderLine1, String HeaderLine2, String HeaderLine3, String HeaderLine4, String HeaderLine5) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + ActionID + "\t";
        InputString = InputString + SerialNumber + "\t";
        InputString = InputString + FMnumber + "\t";
        InputString = InputString + TAXnumber + "\t";
        InputString = InputString + TAXlabel + "\t";
        InputString = InputString + TechID + "\t";
        InputString = InputString + NexusCode + "\t";
        InputString = InputString + HeaderLine1 + "\t";
        InputString = InputString + HeaderLine2 + "\t";
        InputString = InputString + HeaderLine3 + "\t";
        InputString = InputString + HeaderLine4 + "\t";
        InputString = InputString + HeaderLine5 + "\t";
        String OutpS = this.customCommand(72, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command73Variant0Version0(String ActionID, String TAXlabel, String TechID, String NexusCode, String HeaderLine1, String HeaderLine2, String HeaderLine3, String HeaderLine4, String HeaderLine5) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + ActionID + "\t";
        InputString = InputString + TAXlabel + "\t";
        InputString = InputString + TechID + "\t";
        InputString = InputString + NexusCode + "\t";
        InputString = InputString + HeaderLine1 + "\t";
        InputString = InputString + HeaderLine2 + "\t";
        InputString = InputString + HeaderLine3 + "\t";
        InputString = InputString + HeaderLine4 + "\t";
        InputString = InputString + HeaderLine5 + "\t";
        String OutpS = this.customCommand(73, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command74Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(74, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("statusBytes", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command76Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(76, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(6);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("isOpen", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("number", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("items", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("refundItems", myOutputList[5]);
        }

        if (myOutputList.length > 5) {
            R.put("amount", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("payed", myOutputList[6]);
        }

        return R;
    }

    public FiscalResponse command80Variant0Version0(String hz, String mSec) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + hz + "\t";
        InputString = InputString + mSec + "\t";
        String OutpS = this.customCommand(80, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command83Variant0Version0(String taxA, String taxB, String taxC, String taxD, String taxE, String taxF, String taxG, String taxH, String decimalPoint) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + taxA + "\t";
        InputString = InputString + taxB + "\t";
        InputString = InputString + taxC + "\t";
        InputString = InputString + taxD + "\t";
        InputString = InputString + taxE + "\t";
        InputString = InputString + taxF + "\t";
        InputString = InputString + taxG + "\t";
        InputString = InputString + taxH + "\t";
        InputString = InputString + decimalPoint + "\t";
        String OutpS = this.customCommand(83, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("remainingChanges", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command84Variant0Version0(String barcodeType, String barcodeData, String qRcodeSize) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + barcodeType + "\t";
        InputString = InputString + barcodeData + "\t";
        String OutpS = this.customCommand(84, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command84Variant1Version0(String barcodeData, String QRcodeSize) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "4\t" + barcodeData + "\t" + QRcodeSize + "\t";
        String OutpS = this.customCommand(84, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command86Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(86, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("dateTime", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command88Variant0Version0(String departmentNumber) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + departmentNumber + "\t";
        String OutpS = this.customCommand(88, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(6);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("TotSales", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("TotSum", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("Name", myOutputList[3]);
        }

        return R;
    }

    public FiscalResponse command89Variant0Version0(String testType) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + testType + "\t";
        String OutpS = this.customCommand(89, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("records", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command90Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "\t";
        String OutpS = this.customCommand(90, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(9);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("device name", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("fwRev", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("fwDate", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("fwTime", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("checksum", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("sw", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("serialNumber", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("fMNumber", myOutputList[8]);
        }

        return R;
    }

    public FiscalResponse command90Variant1Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "1\t";
        String OutpS = this.customCommand(90, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(9);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("device name", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("fwRev", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("fwDate", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("fwTime", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("checksum", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("sw", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("serialNumber", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("fMNumber", myOutputList[8]);
        }

        return R;
    }

    public FiscalResponse command91Variant0Version0(String serialNumber, String fMnumber) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + serialNumber + "\t";
        InputString = InputString + fMnumber + "\t";
        String OutpS = this.customCommand(91, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("country", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command92Variant0Version0(String type) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + type + "\t";
        String OutpS = this.customCommand(92, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command94Variant0Version0(String type, String startDate, String endDate) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + type + "\t";
        InputString = InputString + startDate + "\t";
        InputString = InputString + endDate + "\t";
        String OutpS = this.customCommand(94, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command95Variant0Version0(String type, String startNumber, String endNumber) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + type + "\t";
        InputString = InputString + startNumber + "\t";
        InputString = InputString + endNumber + "\t";
        String OutpS = this.customCommand(95, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command99Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(99, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("tAXnumber", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command100Variant0Version0(String code) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + code + "\t";
        String OutpS = this.customCommand(100, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(3);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("code", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("errorMessage", myOutputList[2]);
        }

        return R;
    }

    public FiscalResponse command101Variant0Version0(String operatorCode, String oldPassword, String newPassword) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + operatorCode + "\t";
        InputString = InputString + oldPassword + "\t";
        InputString = InputString + newPassword + "\t";
        String OutpS = this.customCommand(101, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command103Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(103, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(11);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("sumVATA", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("sumVATB", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("sumVATC", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("sumVATD", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("sumVATE", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("sumVATF", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("sumVATG", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("sumVATH", myOutputList[8]);
        }

        if (myOutputList.length > 10) {
            R.put("InvNum", myOutputList[9]);
        }

        if (myOutputList.length > 11) {
            R.put("fStorno", myOutputList[10]);
        }

        return R;
    }

    public FiscalResponse command105Variant0Version0(String firstOper, String lastOper, String clear) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + firstOper + "\t";
        InputString = InputString + lastOper + "\t";
        InputString = InputString + clear + "\t";
        String OutpS = this.customCommand(105, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command106Variant0Version0(String mSec) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + mSec + "\t";
        String OutpS = this.customCommand(106, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command107Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "I";
        InputString = InputString + "\t";
        String OutpS = this.customCommand(107, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(3);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("total", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("prog", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("nameLen", myOutputList[3]);
        }

        return R;
    }

    public FiscalResponse command107Variant1Version0(String PLU, String taxGr, String dep, String group, String priceType, String price, String addQty, String quantity, String bar1, String bar2, String bar3, String bar4, String itemName) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "P";
        InputString = InputString + "\t";
        InputString = InputString + PLU + "\t";
        InputString = InputString + taxGr + "\t";
        InputString = InputString + dep + "\t";
        InputString = InputString + group + "\t";
        InputString = InputString + priceType + "\t";
        InputString = InputString + price + "\t";
        InputString = InputString + addQty + "\t";
        InputString = InputString + quantity + "\t";
        InputString = InputString + bar1 + "\t";
        InputString = InputString + bar2 + "\t";
        InputString = InputString + bar3 + "\t";
        InputString = InputString + bar4 + "\t";
        InputString = InputString + itemName + "\t";
        String OutpS = this.customCommand(107, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command107Variant1Version1(String PLU, String taxGr, String dep, String group, String priceType, String price, String addQty, String quantity, String bar1, String bar2, String bar3, String bar4, String itemName, String unitIndex) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "P";
        InputString = InputString + "\t";
        InputString = InputString + PLU + "\t";
        InputString = InputString + taxGr + "\t";
        InputString = InputString + dep + "\t";
        InputString = InputString + group + "\t";
        InputString = InputString + priceType + "\t";
        InputString = InputString + price + "\t";
        InputString = InputString + addQty + "\t";
        InputString = InputString + quantity + "\t";
        InputString = InputString + bar1 + "\t";
        InputString = InputString + bar2 + "\t";
        InputString = InputString + bar3 + "\t";
        InputString = InputString + bar4 + "\t";
        InputString = InputString + itemName + "\t";
        InputString = InputString + unitIndex + "\t";
        String OutpS = this.customCommand(107, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command107Variant2Version0(String PLU, String quantity) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "A";
        InputString = InputString + "\t";
        InputString = InputString + PLU + "\t";
        InputString = InputString + quantity + "\t";
        String OutpS = this.customCommand(107, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command107Variant3Version0(String startPLU, String endPLU) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "D";
        InputString = InputString + "\t";
        InputString = InputString + startPLU + "\t";
        InputString = InputString + endPLU + "\t";
        String OutpS = this.customCommand(107, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command107Variant3Version1() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "D\tA\t";
        String OutpS = this.customCommand(107, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command107Variant4Version0(String option, String PLU) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + option + "\t";
        InputString = InputString + PLU + "\t";
        String OutpS = this.customCommand(107, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(15);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("PLU", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("taxGr", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("department", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("stockGroup", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("priceType", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("price", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("turnover", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("soldQty", myOutputList[8]);
        }

        if (myOutputList.length > 9) {
            R.put("stockQty", myOutputList[9]);
        }

        if (myOutputList.length > 10) {
            R.put("bar1", myOutputList[10]);
        }

        if (myOutputList.length > 11) {
            R.put("bar2", myOutputList[11]);
        }

        if (myOutputList.length > 12) {
            R.put("bar3", myOutputList[12]);
        }

        if (myOutputList.length > 13) {
            R.put("bar4", myOutputList[13]);
        }

        if (myOutputList.length > 14) {
            R.put("itemName", myOutputList[14]);
        }

        return R;
    }

    public FiscalResponse command107Variant5Version0(String option, String PLU) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + option + "\t";
        InputString = InputString + PLU + "\t";
        String OutpS = this.customCommand(107, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(15);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("PLU", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("taxGr", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("department", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("stockGroup", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("priceType", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("price", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("turnover", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("soldQty", myOutputList[8]);
        }

        if (myOutputList.length > 9) {
            R.put("stockQty", myOutputList[9]);
        }

        if (myOutputList.length > 10) {
            R.put("bar1", myOutputList[10]);
        }

        if (myOutputList.length > 11) {
            R.put("bar2", myOutputList[11]);
        }

        if (myOutputList.length > 12) {
            R.put("bar3", myOutputList[12]);
        }

        if (myOutputList.length > 13) {
            R.put("bar4", myOutputList[13]);
        }

        if (myOutputList.length > 14) {
            R.put("itemName", myOutputList[14]);
        }

        if (myOutputList.length > 15) {
            R.put("unitIndex", myOutputList[15]);
        }

        return R;
    }

    public FiscalResponse command107Variant6Version0(String option) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + option + "\t";
        String OutpS = this.customCommand(107, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(15);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("PLU", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("taxGr", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("department", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("stockGroup", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("priceType", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("price", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("turnover", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("soldQty", myOutputList[8]);
        }

        if (myOutputList.length > 9) {
            R.put("stockQty", myOutputList[9]);
        }

        if (myOutputList.length > 10) {
            R.put("bar1", myOutputList[10]);
        }

        if (myOutputList.length > 11) {
            R.put("bar2", myOutputList[11]);
        }

        if (myOutputList.length > 12) {
            R.put("bar3", myOutputList[12]);
        }

        if (myOutputList.length > 13) {
            R.put("bar4", myOutputList[13]);
        }

        if (myOutputList.length > 14) {
            R.put("itemName", myOutputList[14]);
        }

        if (myOutputList.length > 15) {
            R.put("unitIndex", myOutputList[15]);
        }

        return R;
    }

    public FiscalResponse command109Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        String OutpS = this.customCommand(109, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command110Variant0Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "0\t";
        String OutpS = this.customCommand(110, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(8);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("pay1", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("pay2", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("pay3", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("pay4", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("pay5", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("pay6", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("foreignPay", myOutputList[7]);
        }

        return R;
    }

    public FiscalResponse command110Variant1Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "1\t";
        String OutpS = this.customCommand(110, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(8);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("pay1", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("pay2", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("pay3", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("pay4", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("pay5", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("pay6", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("foreignPay", myOutputList[7]);
        }

        return R;
    }

    public FiscalResponse command110Variant2Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "2\t";
        String OutpS = this.customCommand(110, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(3);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("num", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("sum", myOutputList[2]);
        }

        return R;
    }

    public FiscalResponse command110Variant3Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "3\t";
        String OutpS = this.customCommand(110, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(5);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("qSur", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("sSur", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("qDis", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("sDis", myOutputList[4]);
        }

        return R;
    }

    public FiscalResponse command110Variant4Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "4\t";
        String OutpS = this.customCommand(110, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(5);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("qVoid", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("sVoid", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("qAnul", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("sAnul", myOutputList[4]);
        }

        return R;
    }

    public FiscalResponse command110Variant5Version0() throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "5\t";
        String OutpS = this.customCommand(110, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(9);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("qCashIn1", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("sCashIn1", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("qCashOut1", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("sCashOut1", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("qCashIn2", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("sCashIn2", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("qCashOut2", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("sCashOut2", myOutputList[8]);
        }

        return R;
    }

    public FiscalResponse command111Variant0Version0(String reportType, String startPLU, String endPLU) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + reportType + "\t";
        InputString = InputString + startPLU + "\t";
        InputString = InputString + endPLU + "\t";
        String OutpS = this.customCommand(111, InputString);
        FiscalResponse R = new FiscalResponse(1);
        R.put("errorCode", OutpS);
        return R;
    }

    public FiscalResponse command112Variant0Version0(String wpOperator) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + wpOperator + "\t";
        String OutpS = this.customCommand(112, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(9);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("receipts", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("total", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("nDiscount", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("discount", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("nSurcharge", myOutputList[5]);
        }

        if (myOutputList.length > 6) {
            R.put("surcharge", myOutputList[6]);
        }

        if (myOutputList.length > 7) {
            R.put("nVoid", myOutputList[7]);
        }

        if (myOutputList.length > 8) {
            R.put("void", myOutputList[8]);
        }

        return R;
    }

    public FiscalResponse command116Variant0Version0(String operation, String address, String nBytes) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + operation + "\t";
        InputString = InputString + address + "\t";
        InputString = InputString + nBytes + "\t";
        String OutpS = this.customCommand(116, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("data", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command124Variant0Version0(String startDate, String endDate, String DocType) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + startDate + "\t";
        InputString = InputString + endDate + "\t";
        InputString = InputString + DocType + "\t";
        String OutpS = this.customCommand(124, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(5);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("startDate", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("endDate", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("firstDoc", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("lastDoc", myOutputList[4]);
        }

        return R;
    }

    public FiscalResponse command125Variant0Version0(String docNum, String recType) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "0\t";
        InputString = InputString + docNum + "\t";
        InputString = InputString + recType + "\t";
        String OutpS = this.customCommand(125, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(6);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("docNumber", myOutputList[1]);
        }

        if (myOutputList.length > 2) {
            R.put("recNumber", myOutputList[2]);
        }

        if (myOutputList.length > 3) {
            R.put("date", myOutputList[3]);
        }

        if (myOutputList.length > 4) {
            R.put("type", myOutputList[4]);
        }

        if (myOutputList.length > 5) {
            R.put("znumber", myOutputList[5]);
        }

        return R;
    }

    public FiscalResponse command125Variant1Version0(String FirstDoc, String LastDoc) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "1\t";
        InputString = InputString + FirstDoc + "\t";
        InputString = InputString + LastDoc + "\t";
        String OutpS = this.customCommand(125, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("TextData", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command125Variant2Version0(String FirstDoc, String LastDoc) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + "2\t";
        InputString = InputString + FirstDoc + "\t";
        InputString = InputString + LastDoc + "\t";
        String OutpS = this.customCommand(125, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("Data", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command125Variant3Version0(String option, String docNum, String recType) throws IOException, FiscalException, IllegalArgumentException {
        String InputString = "";
        InputString = InputString + option + "\t";
        InputString = InputString + docNum + "\t";
        InputString = InputString + recType + "\t";
        String OutpS = this.customCommand(125, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(1);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        return R;
    }

    public FiscalResponse command127Variant0Version0(String typeOper, String stampName) throws IOException, FiscalException {
        String InputString = typeOper + "\t" + stampName + "\t";
        String OutpS = this.customCommand(127, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        return R;
    }

    public FiscalResponse command202Variant1Version0(String data) throws IOException, FiscalException {
        String InputString = data + "\t";
        String OutpS = this.customCommand(202, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("Chechsum", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command202Variant0Version0(String data) throws IOException, FiscalException {
        if (this.getChkInputParams()) {
        }

        String InputString = data + "\t";
        String OutpS = this.customCommand(202, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("Chechsum", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse command203Variant1Version0(String data) throws IOException, FiscalException {
        if (this.getChkInputParams()) {
        }

        String InputString = data + "\t";
        String OutpS = this.customCommand(203, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        return R;
    }

    public FiscalResponse command203Variant0Version0(String data) throws IOException, FiscalException {
        if (this.getChkInputParams()) {
        }

        String InputString = data + "\t";
        String OutpS = this.customCommand(203, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("Chechsum", myOutputList[1]);
        }

        return R;
    }

    protected FiscalResponse read(cmd255Name name, String cmdIndex) throws FiscalException, IOException {
        String nameAsString = name.name();
        String InputString = nameAsString + "\t" + cmdIndex + "\t\t";
        String OutpS = this.customCommand(255, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("VarValue", myOutputList[1]);
        }

        return R;
    }

    protected FiscalResponse write(cmd255Name name, String cmdIndex, String cmdValue) throws FiscalException, IOException {
        if (cmdValue.length() == 0) {
            cmdValue = " ";
        }

        String nameAsString = name.name();
        String InputString = nameAsString + "\t" + cmdIndex + "\t" + cmdValue + "\t";
        String OutpS = this.customCommand(255, InputString);
        String[] myOutputList = split(OutpS, new String[]{"\t"});
        FiscalResponse R = new FiscalResponse(2);
        if (myOutputList.length > 0) {
            R.put("errorCode", myOutputList[0]);
        }

        if (myOutputList.length > 1) {
            R.put("VarValue", myOutputList[1]);
        }

        return R;
    }

    public FiscalResponse Command255Read(cmd255Name cmdName, String cmdIndex) throws FiscalException, IOException {
        return this.read(cmdName, cmdIndex);
    }

    public FiscalResponse Command255Write(cmd255Name cmdName, String cmdIndex, String cmdValue) throws FiscalException, IOException {
        String PATTERN_IP = "^|((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        String PATTERN_MAC = "^|([0-9A-Fa-f]{12})$";
        switch(cmdName) {
            case FpComBaudRate:
                return this.write(cmdName, cmdIndex, cmdValue);
            case ComPortBaudRate:
                return this.write(cmdName, cmdIndex, cmdValue);
            case ComPortProtocol:
                return this.write(cmdName, cmdIndex, cmdValue);
            case BthDiscoverability:
                return this.write(cmdName, cmdIndex, cmdValue);
            case BthPairing:
                return this.write(cmdName, cmdIndex, cmdValue);
            case BthPinCode:
                return this.write(cmdName, cmdIndex, cmdValue);
            case BthAddress:
                return this.write(cmdName, cmdIndex, cmdValue);
            case BarcodeName:
                return this.write(cmdName, cmdIndex, cmdValue);
            case EcrConnectedOperReport:
                return this.write(cmdName, cmdIndex, cmdValue);
            case EcrConnectedDeptReport:
                return this.write(cmdName, cmdIndex, cmdValue);
            case EcrConnectedGroupsReport:
                return this.write(cmdName, cmdIndex, cmdValue);
            case EcrConnectedCashReport:
                return this.write(cmdName, cmdIndex, cmdValue);
            case EcrPluDailyClearing:
                return this.write(cmdName, cmdIndex, cmdValue);
            case AutoPowerOff:
                return this.write(cmdName, cmdIndex, cmdValue);
            case BkLight_AutoOff:
                return this.write(cmdName, cmdIndex, cmdValue);
            case RegModeOnIdle:
                return this.write(cmdName, cmdIndex, cmdValue);
            case WorkBatteryIncluded:
                return this.write(cmdName, cmdIndex, cmdValue);
            case CurrNameLocal:
                if (this.getChkInputParams() && !cmdValue.matches("^.{1,3}$")) {
                    throw new IllegalArgumentException("Wrong input data.Local currency name( up to 3 chars )");
                }

                return this.write(cmdName, cmdIndex, cmdValue);
            case CurrNameForeign:
                if (this.getChkInputParams() && !cmdValue.matches("^.{1,3}$")) {
                    throw new IllegalArgumentException("Wrong input data. Foreign currency name( up to 3 chars )");
                }

                return this.write(cmdName, cmdIndex, cmdValue);
            case ExchangeRate:
                if (this.getChkInputParams() && !cmdValue.matches("^[0-9]{1,9}$")) {
                    throw new IllegalArgumentException("Wrong input data. Exchange rate is not( from 0 to 999999999);");
                }

                return this.write(cmdName, cmdIndex, cmdValue);
            case Header:
                return this.write(cmdName, cmdIndex, cmdValue);
            case Footer:
                return this.write(cmdName, cmdIndex, cmdValue);
            case OperName:
                return this.write(cmdName, cmdIndex, cmdValue);
            case OperPasw:
                return this.write(cmdName, cmdIndex, cmdValue);
            case PayName:
                return this.write(cmdName, cmdIndex, cmdValue);
            case Payment_forbidden:
                return this.write(cmdName, cmdIndex, cmdValue);
            case ServPasw:
                return this.write(cmdName, cmdIndex, cmdValue);
            case ServiceDate:
                return this.write(cmdName, cmdIndex, cmdValue);
            case PrnQuality:
                return this.write(cmdName, cmdIndex, cmdValue);
            case DublReceipts:
                return this.write(cmdName, cmdIndex, cmdValue);
            case BarcodePrint:
                return this.write(cmdName, cmdIndex, cmdValue);
            case LogoPrint:
                return this.write(cmdName, cmdIndex, cmdValue);
            case ForeignPrint:
                return this.write(cmdName, cmdIndex, cmdValue);
            case DHCPenable:
                return this.write(cmdName, cmdIndex, cmdValue);
            case LAN_IP:
                if (this.getChkInputParams() && !cmdValue.matches("^|((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$")) {
                    throw new IllegalArgumentException("Wrong input data. IP not match XXX.XXX.XXX.XXX");
                }

                return this.write(cmdName, cmdIndex, cmdValue);
            case LAN_NetMask:
                if (this.getChkInputParams() && !cmdValue.matches("^|((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$")) {
                    throw new IllegalArgumentException("Wrong input data. NetMask not match XXX.XXX.XXX.XXX ");
                }

                return this.write(cmdName, cmdIndex, cmdValue);
            case LAN_Gateway:
                if (this.getChkInputParams() && !cmdValue.matches("^|((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$")) {
                    throw new IllegalArgumentException("Wrong input data.Gateway not match XXX.XXX.XXX.XXX ");
                }

                return this.write(cmdName, cmdIndex, cmdValue);
            case LAN_PriDNS:
                if (this.getChkInputParams() && !cmdValue.matches("^|((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$")) {
                    throw new IllegalArgumentException("Wrong input data. PriDNS not match XXX.XXX.XXX.XXX ");
                }

                return this.write(cmdName, cmdIndex, cmdValue);
            case LAN_SecDNS:
                if (this.getChkInputParams() && !cmdValue.matches("^|((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$")) {
                    throw new IllegalArgumentException("Wrong input data. SecDNS not match XXX.XXX.XXX.XXX ");
                }

                return this.write(cmdName, cmdIndex, cmdValue);
            default:
                return this.write(cmdName, cmdIndex, cmdValue);
        }
    }

    public String[] getPrintColumnsSupported() {
        return this.printColumnsSupported;
    }

    public boolean getCapAutoCutter() {
        return false;
    }

    public boolean getCapFontSupprted() {
        return this.capFontSupprted;
    }

    public boolean getCapBarcodeSupprted() {
        return this.capBarcodeSupported;
    }

    public Integer getMAX_PLU() {
        return this.MAX_PLU;
    }

    public int getMaxNonFiscalText() {
        return 42;
    }

    public int getMaxTextHeaderFooter() {
        return 42;
    }

    public boolean isCapHasCutter() {
        return false;
    }

    public boolean isCapFontSupprted() {
        return this.capFontSupprted;
    }

    public boolean isCapBarcodeSupported() {
        return this.capBarcodeSupported;
    }
}
