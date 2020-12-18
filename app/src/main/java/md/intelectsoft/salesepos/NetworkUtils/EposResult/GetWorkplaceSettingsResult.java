
package md.intelectsoft.salesepos.NetworkUtils.EposResult;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import md.intelectsoft.salesepos.NetworkUtils.FiscalDevice;
import md.intelectsoft.salesepos.NetworkUtils.PaymentType;

public class GetWorkplaceSettingsResult {

    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorText")
    @Expose
    private String errorText;
    @SerializedName("FiscalDevice")
    @Expose
    private FiscalDevice fiscalDevice;
    @SerializedName("PaymentTypes")
    @Expose
    private List<PaymentType> paymentTypes = null;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public FiscalDevice getFiscalDevice() {
        return fiscalDevice;
    }

    public void setFiscalDevice(FiscalDevice fiscalDevice) {
        this.fiscalDevice = fiscalDevice;
    }

    public List<PaymentType> getPaymentTypes() {
        return paymentTypes;
    }

    public void setPaymentTypes(List<PaymentType> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

}
