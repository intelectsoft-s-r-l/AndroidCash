package md.intelectsoft.salesepos.NetworkUtils.EposResult;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 04.02.2020
 */

public class AuthentificateUserResult {
    @SerializedName("AuthentificateUserResult")
    @Expose
    private TokenReceivedFromAutenficateUser authentificateUserResult;

    public TokenReceivedFromAutenficateUser getAuthentificateUserResult() {
        return authentificateUserResult;
    }

    public void setAuthentificateUserResult(TokenReceivedFromAutenficateUser authentificateUserResult) {
        this.authentificateUserResult = authentificateUserResult;
    }
}
