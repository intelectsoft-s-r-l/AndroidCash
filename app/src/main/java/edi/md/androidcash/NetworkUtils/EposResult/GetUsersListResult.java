
package edi.md.androidcash.NetworkUtils.EposResult;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import edi.md.androidcash.NetworkUtils.User;

public class GetUsersListResult {

    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorText")
    @Expose
    private Object errorText;
    @SerializedName("Users")
    @Expose
    private List<User> users = null;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Object getErrorText() {
        return errorText;
    }

    public void setErrorText(Object errorText) {
        this.errorText = errorText;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
