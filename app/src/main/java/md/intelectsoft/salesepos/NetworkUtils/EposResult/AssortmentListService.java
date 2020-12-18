
package md.intelectsoft.salesepos.NetworkUtils.EposResult;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AssortmentListService {

    @SerializedName("GetAssortmentListResult")
    @Expose
    private GetAssortmentListResult getAssortmentListResult;

    public GetAssortmentListResult getGetAssortmentListResult() {
        return getAssortmentListResult;
    }

    public void setGetAssortmentListResult(GetAssortmentListResult getAssortmentListResult) {
        this.getAssortmentListResult = getAssortmentListResult;
    }

}
