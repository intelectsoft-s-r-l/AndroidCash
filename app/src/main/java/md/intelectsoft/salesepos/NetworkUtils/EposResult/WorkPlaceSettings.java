
package md.intelectsoft.salesepos.NetworkUtils.EposResult;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WorkPlaceSettings {

    @SerializedName("GetWorkplaceSettingsResult")
    @Expose
    private GetWorkplaceSettingsResult getWorkplaceSettingsResult;

    public GetWorkplaceSettingsResult getGetWorkplaceSettingsResult() {
        return getWorkplaceSettingsResult;
    }

    public void setGetWorkplaceSettingsResult(GetWorkplaceSettingsResult getWorkplaceSettingsResult) {
        this.getWorkplaceSettingsResult = getWorkplaceSettingsResult;
    }

}
