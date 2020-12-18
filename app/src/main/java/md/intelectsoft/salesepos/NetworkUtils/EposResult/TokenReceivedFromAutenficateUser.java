package md.intelectsoft.salesepos.NetworkUtils.EposResult;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 04.02.2020
 */

public class TokenReceivedFromAutenficateUser {
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorText")
    @Expose
    private String errorText;
    @SerializedName("Token")
    @Expose
    private String token;
    @SerializedName("TokenValidTo")
    @Expose
    private String tokenValidTo;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenValidTo() {
        return tokenValidTo;
    }

    public void setTokenValidTo(String tokenValidTo) {
        this.tokenValidTo = tokenValidTo;
    }
}
