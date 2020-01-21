package edi.md.androidcash.NetworkUtils.RetrofitRemote;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.Body.BodyRegisterApp;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.RegisterApplicationResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Igor on 25.11.2019
 */

public interface RegisterAppService {
    @POST("/BrokerService/json/RegisterApplication")
    Call<RegisterApplicationResult> registerApplicationCall(@Body BodyRegisterApp bodyRegisterApp);
}
