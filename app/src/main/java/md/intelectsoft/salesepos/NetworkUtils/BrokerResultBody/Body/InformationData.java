package md.intelectsoft.salesepos.NetworkUtils.BrokerResultBody.Body;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InformationData {
    @SerializedName("Information")
    @Expose
    private String information;
    @SerializedName("LicenseID")
    @Expose
    private String licenseID;

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getLicenseID() {
        return licenseID;
    }

    public void setLicenseID(String licenseID) {
        this.licenseID = licenseID;
    }
}
