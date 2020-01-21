package edi.md.androidcash.FiscalService;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrintBillFiscalService {
    @SerializedName("DiscountSum")
    @Expose
    private Double discountSum;
    @SerializedName("FooterText")
    @Expose
    private String footerText;
    @SerializedName("HeaderText")
    @Expose
    private String headerText;
    @SerializedName("Lines")
    @Expose
    private List<BillLineFiscalService> lines = null;
    @SerializedName("Number")
    @Expose
    private String number;
    @SerializedName("Payments")
    @Expose
    private List<BillPaymentFiscalService> payments = null;

    public Double getDiscountSum() {
        return discountSum;
    }

    public void setDiscountSum(Double discountSum) {
        this.discountSum = discountSum;
    }

    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    public List<BillLineFiscalService> getLines() {
        return lines;
    }

    public void setLines(List<BillLineFiscalService> lines) {
        this.lines = lines;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<BillPaymentFiscalService> getPayments() {
        return payments;
    }

    public void setPayments(List<BillPaymentFiscalService> payments) {
        this.payments = payments;
    }
}
