package edi.md.androidcash.NetworkUtils.BrokerResultBody.Body;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.11.2019
 */

public class BodyGetURI {
    @SerializedName("activatecode")
    @Expose
    private String activatecode;

    @SerializedName("install_id")
    @Expose
    private String install_id;

    public String getActivateCode() {
        return activatecode;
    }

    public void setActivateCode(String activatecode) {
        this.activatecode = activatecode;
    }

    public String getInstall_id() {
        return install_id;
    }

    public void setInstall_id(String install_id) {
        this.install_id = install_id;
    }
}
