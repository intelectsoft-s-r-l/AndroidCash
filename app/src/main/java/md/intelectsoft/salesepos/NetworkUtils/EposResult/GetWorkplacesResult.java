
package md.intelectsoft.salesepos.NetworkUtils.EposResult;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import md.intelectsoft.salesepos.NetworkUtils.WorkplaceEntry;

public class GetWorkplacesResult {

    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorText")
    @Expose
    private String errorText;
    @SerializedName("Workplaces")
    @Expose
    private List<WorkplaceEntry> workplaces = null;

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

    public List<WorkplaceEntry> getWorkplaces() {
        return workplaces;
    }

    public void setWorkplaces(List<WorkplaceEntry> workplaces) {
        this.workplaces = workplaces;
    }

}
