package edi.md.androidcash.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SendBillsToServer {
    @SerializedName("Bills")
    @Expose
    private List<SaveBill> bills = null;
    @SerializedName("ShiftID")
    @Expose
    private String shiftID;
    @SerializedName("Token")
    @Expose
    private String token;

    public List<SaveBill> getBills() {
        return bills;
    }

    public void setBills(List<SaveBill> bills) {
        this.bills = bills;
    }

    public String getShiftID() {
        return shiftID;
    }

    public void setShiftID(String shiftID) {
        this.shiftID = shiftID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
