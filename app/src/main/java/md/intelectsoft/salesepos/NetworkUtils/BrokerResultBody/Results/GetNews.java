package md.intelectsoft.salesepos.NetworkUtils.BrokerResultBody.Results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetNews {

    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("NewsList")
    @Expose
    private List<NewsList> newsList = null;

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

    public List<NewsList> getNewsList() {
        return newsList;
    }

    public void setNewsList(List<NewsList> newsList) {
        this.newsList = newsList;
    }
}
