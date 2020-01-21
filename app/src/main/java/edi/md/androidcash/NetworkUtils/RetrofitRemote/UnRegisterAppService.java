package edi.md.androidcash.NetworkUtils.RetrofitRemote;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.Body.BodyUnRegisterApp;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.UnRegisterApplicationResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Igor on 25.11.2019
 */

public interface UnRegisterAppService {
    @POST("/BrokerService/json/UnRegisterApplication")
    Call<UnRegisterApplicationResult> unRegisterApplicationCall(@Body BodyUnRegisterApp bodyUnRegisterApp);
}
