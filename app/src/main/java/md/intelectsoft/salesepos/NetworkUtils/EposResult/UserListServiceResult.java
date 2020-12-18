
package md.intelectsoft.salesepos.NetworkUtils.EposResult;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserListServiceResult {

    @SerializedName("GetUsersListResult")
    @Expose
    private GetUsersListResult getUsersListResult;

    public GetUsersListResult getGetUsersListResult() {
        return getUsersListResult;
    }

    public void setGetUsersListResult(GetUsersListResult getUsersListResult) {
        this.getUsersListResult = getUsersListResult;
    }

}
