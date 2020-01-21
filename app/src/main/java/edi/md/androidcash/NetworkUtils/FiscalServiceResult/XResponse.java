package edi.md.androidcash.NetworkUtils.FiscalServiceResult;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class XResponse {
    @SerializedName("PrintReportXResult")
    @Expose
    private PrintReportXResult printReportXResult;

    public PrintReportXResult getPrintReportXResult() {
        return printReportXResult;
    }

    public void setPrintReportXResult(PrintReportXResult printReportXResult) {
        this.printReportXResult = printReportXResult;
    }
}
