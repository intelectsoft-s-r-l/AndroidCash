package md.intelectsoft.brokerservice.results;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.11.2019
 */

public class RegisterApplication {
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("AppData")
    @Expose
    private AppDataRegisterApplication appData;

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

    public AppDataRegisterApplication getAppData() {
        return appData;
    }

    public void setAppData(AppDataRegisterApplication appData) {
        this.appData = appData;
    }
}
