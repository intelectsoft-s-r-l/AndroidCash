package md.intelectsoft.salesepos.NetworkUtils.BrokerResultBody.Results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppDataRegisterApplication {
    @SerializedName("Company")
    @Expose
    private String company;
    @SerializedName("IDNO")
    @Expose
    private String iDNO;
    @SerializedName("LicenseCode")
    @Expose
    private String licenseCode;
    @SerializedName("LicenseID")
    @Expose
    private String licenseID;
    @SerializedName("ServerDateTime")
    @Expose
    private String serverDateTime;
    @SerializedName("URI")
    @Expose
    private String uri;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getIDNO() {
        return iDNO;
    }

    public void setIDNO(String iDNO) {
        this.iDNO = iDNO;
    }

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    public String getLicenseID() {
        return licenseID;
    }

    public void setLicenseID(String licenseID) {
        this.licenseID = licenseID;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uRI) {
        this.uri = uRI;
    }

    public String getServerDateTime() {
        return serverDateTime;
    }

    public void setServerDateTime(String serverDateTime) {
        this.serverDateTime = serverDateTime;
    }
}