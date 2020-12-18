package md.intelectsoft.salesepos.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SaveBill {
    @SerializedName("ClosedByID")
    @Expose
    private String closedByID;
    @SerializedName("ClosingDate")
    @Expose
    private String closingDate;
    @SerializedName("CreationDate")
    @Expose
    private String creationDate;
    @SerializedName("DiscountCardId")
    @Expose
    private String discountCardId;
    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("Lines")
    @Expose
    private List<SaveBillLine> lines = null;
    @SerializedName("Number")
    @Expose
    private Integer number;
    @SerializedName("OpenedByID")
    @Expose
    private String openedByID;
    @SerializedName("Payments")
    @Expose
    private List<SaveBillPayment> payments = null;

    public String getClosedByID() {
        return closedByID;
    }

    public void setClosedByID(String closedByID) {
        this.closedByID = closedByID;
    }

    public String getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(String closingDate) {
        this.closingDate = closingDate;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getDiscountCardId() {
        return discountCardId;
    }

    public void setDiscountCardId(String discountCardId) {
        this.discountCardId = discountCardId;
    }

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public List<SaveBillLine> getLines() {
        return lines;
    }

    public void setLines(List<SaveBillLine> lines) {
        this.lines = lines;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getOpenedByID() {
        return openedByID;
    }

    public void setOpenedByID(String openedByID) {
        this.openedByID = openedByID;
    }

    public List<SaveBillPayment> getPayments() {
        return payments;
    }

    public void setPayments(List<SaveBillPayment> payments) {
        this.payments = payments;
    }
}
