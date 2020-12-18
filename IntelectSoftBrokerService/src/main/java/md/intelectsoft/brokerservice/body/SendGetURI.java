package md.intelectsoft.brokerservice.body;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.11.2019
 */

public class SendGetURI {
    @SerializedName("ApplicationVersion")
    @Expose
    private String applicationVersion;
    @SerializedName("DeviceID")
    @Expose
    private String deviceID;
    @SerializedName("DeviceName")
    @Expose
    private String deviceName;
    @SerializedName("DeviceModel")
    @Expose
    private String deviceModel;
    @SerializedName("LicenseID")
    @Expose
    private String licenseID;
    @SerializedName("OSType")
    @Expose
    private Integer oSType;
    @SerializedName("OSVersion")
    @Expose
    private String oSVersion;
    @SerializedName("PrivateIP")
    @Expose
    private String privateIP;
    @SerializedName("ProductType")
    @Expose
    private Integer productType;
    @SerializedName("PublicIP")
    @Expose
    private String publicIP;
    @SerializedName("SerialNumber")
    @Expose
    private String serialNumber;
    @SerializedName("Workplace")
    @Expose
    private String workPlace;
    @SerializedName("SalePointAddress")
    @Expose
    private String salePointAddress;

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getLicenseID() {
        return licenseID;
    }

    public void setLicenseID(String licenseID) {
        this.licenseID = licenseID;
    }

    public Integer getOSType() {
        return oSType;
    }

    public void setOSType(Integer oSType) {
        this.oSType = oSType;
    }

    public String getOSVersion() {
        return oSVersion;
    }

    public void setOSVersion(String oSVersion) {
        this.oSVersion = oSVersion;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    public void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }

    public Integer getProductType() {
        return productType;
    }

    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getWorkPlace() {
        return workPlace;
    }

    public void setWorkPlace(String workPlace) {
        this.workPlace = workPlace;
    }

    public String getSalePointAddress() {
        return salePointAddress;
    }

    public void setSalePointAddress(String salePointAddress) {
        this.salePointAddress = salePointAddress;
    }
}
