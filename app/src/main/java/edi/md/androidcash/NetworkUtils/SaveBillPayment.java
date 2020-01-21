package edi.md.androidcash.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SaveBillPayment {

    @SerializedName("CreatedByID")
    @Expose
    private String createdByID;
    @SerializedName("CreationDate")
    @Expose
    private String creationDate;
    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("PaymentTypeID")
    @Expose
    private String paymentTypeID;
    @SerializedName("Sum")
    @Expose
    private Double sum;

    public String getCreatedByID() {
        return createdByID;
    }

    public void setCreatedByID(String createdByID) {
        this.createdByID = createdByID;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getPaymentTypeID() {
        return paymentTypeID;
    }

    public void setPaymentTypeID(String paymentTypeID) {
        this.paymentTypeID = paymentTypeID;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }
}
