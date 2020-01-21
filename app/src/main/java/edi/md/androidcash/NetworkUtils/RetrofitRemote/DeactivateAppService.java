package edi.md.androidcash.NetworkUtils.RetrofitRemote;

import edi.md.androidcash.NetworkUtils.BrokerResultBody.Body.BodyDeactivateApp;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.DeactivateApplicationResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Igor on 25.11.2019
 */

public interface DeactivateAppService {
    @POST("/BrokerService/json/DeactivateApplication")
    Call<DeactivateApplicationResult> deactivateApplicationCall(@Body BodyDeactivateApp bodyDeactivateApp);
}
