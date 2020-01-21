package edi.md.androidcash.NetworkUtils.BrokerResultBody.Body;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.11.2019
 */

public class BodyDeactivateApp {
    @SerializedName("email")
    @Expose
    private int email;

    @SerializedName("password")
    @Expose
    private int password;

    public int getEmail() {
        return email;
    }

    public void setEmail(int email) {
        this.email = email;
    }

    public int getPassword() {
        return password;
    }

    public void setPassword(int password) {
        this.password = password;
    }
}
