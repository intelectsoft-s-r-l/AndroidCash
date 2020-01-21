package edi.md.androidcash.FiscalService;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BillPaymentFiscalService {
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("PaymentSum")
    @Expose
    private Double paymentSum;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getPaymentSum() {
        return paymentSum;
    }

    public void setPaymentSum(Double paymentSum) {
        this.paymentSum = paymentSum;
    }
}
