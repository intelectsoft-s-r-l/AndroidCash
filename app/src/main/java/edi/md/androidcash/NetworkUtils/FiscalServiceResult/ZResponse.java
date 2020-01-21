package edi.md.androidcash.NetworkUtils.FiscalServiceResult;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ZResponse {
    @SerializedName("PrintReportZResult")
    @Expose
    private PrintReportZResult printReportZResult;

    public PrintReportZResult getPrintReportZResult() {
        return printReportZResult;
    }

    public void setPrintReportZResult(PrintReportZResult printReportZResult) {
        this.printReportZResult = printReportZResult;
    }
}
