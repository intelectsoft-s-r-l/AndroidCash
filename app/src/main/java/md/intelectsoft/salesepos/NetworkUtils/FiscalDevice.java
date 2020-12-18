package md.intelectsoft.salesepos.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class FiscalDevice extends RealmObject {

    @SerializedName("AdditionalParameters")
    @Expose
    private String additionalParameters;
    @SerializedName("Model")
    @Expose
    private Integer model;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Password")
    @Expose
    private String password;
    @SerializedName("PortNumber")
    @Expose
    private Integer portNumber;
    @SerializedName("PortSpeed")
    @Expose
    private Integer portSpeed;
    @SerializedName("ServiceUri")
    @Expose
    private String serviceUri;

    public String getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(String additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    public Integer getModel() {
        return model;
    }

    public void setModel(Integer model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }

    public Integer getPortSpeed() {
        return portSpeed;
    }

    public void setPortSpeed(Integer portSpeed) {
        this.portSpeed = portSpeed;
    }

    public String getServiceUri() {
        return serviceUri;
    }

    public void setServiceUri(String serviceUri) {
        this.serviceUri = serviceUri;
    }

}
