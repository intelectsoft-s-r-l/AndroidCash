
package md.intelectsoft.salesepos.NetworkUtils.EposResult;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetWorkPlaceService {

    @SerializedName("GetWorkplacesResult")
    @Expose
    private GetWorkplacesResult getWorkplacesResult;

    public GetWorkplacesResult getGetWorkplacesResult() {
        return getWorkplacesResult;
    }

    public void setGetWorkplacesResult(GetWorkplacesResult getWorkplacesResult) {
        this.getWorkplacesResult = getWorkplacesResult;
    }

}
