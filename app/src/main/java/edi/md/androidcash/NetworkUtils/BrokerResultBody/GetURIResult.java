package edi.md.androidcash.NetworkUtils.BrokerResultBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.11.2019
 */

public class GetURIResult {
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("instalationidvalidto")
    @Expose
    private String instalationidvalidto;
    @SerializedName("DateNow")
    @Expose
    private String dateNow;
    @SerializedName("uri")
    @Expose
    private String uri;

    public String getDateNow() {
        return dateNow;
    }

    public void setDateNow(String dateNow) {
        this.dateNow = dateNow;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getInstalationidvalidto() {
        return instalationidvalidto;
    }
    public void setInstalationidvalidto(String instalationidvalidto) {
        this.instalationidvalidto = instalationidvalidto;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
