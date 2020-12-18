package md.intelectsoft.salesepos.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SaveShift {

    @SerializedName("CashID")
    @Expose
    private String cashID;
    @SerializedName("Closed")
    @Expose
    private Boolean closed;
    @SerializedName("ClosedByID")
    @Expose
    private String closedByID;
    @SerializedName("ClosingDate")
    @Expose
    private String closingDate;
    @SerializedName("CreationDate")
    @Expose
    private String creationDate;
    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("OpenedById")
    @Expose
    private String openedById;

    public String getCashID() {
        return cashID;
    }

    public void setCashID(String cashID) {
        this.cashID = cashID;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

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

    public String getiD() {
        return iD;
    }

    public void setiD(String iD) {
        this.iD = iD;
    }

    public String getOpenedById() {
        return openedById;
    }

    public void setOpenedById(String openedById) {
        this.openedById = openedById;
    }
}
