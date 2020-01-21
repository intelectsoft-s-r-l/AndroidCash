package edi.md.androidcash.NetworkUtils.BrokerResultBody.Body;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.11.2019
 */

public class BodyRegisterApp {
    @SerializedName("DeviceID")
    @Expose
    private String deviceId;

    @SerializedName("Platform")
    @Expose
    private int platform;

    @SerializedName("ProductType")
    @Expose
    private int productType;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("idno")
    @Expose
    private String idno;

    @SerializedName("password")
    @Expose
    private String password;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public int getProductType() {
        return productType;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdno() {
        return idno;
    }

    public void setIdno(String idno) {
        this.idno = idno;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
