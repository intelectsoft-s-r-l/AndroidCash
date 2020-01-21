package edi.md.androidcash.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SendShiftToServer {

    @SerializedName("Shift")
    @Expose
    private SaveShift shift;
    @SerializedName("Token")
    @Expose
    private String token;

    public SaveShift getShift() {
        return shift;
    }

    public void setShift(SaveShift shift) {
        this.shift = shift;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
