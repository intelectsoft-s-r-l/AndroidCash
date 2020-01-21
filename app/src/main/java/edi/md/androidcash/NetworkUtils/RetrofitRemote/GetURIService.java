package edi.md.androidcash.NetworkUtils.RetrofitRemote;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.GetURIResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Igor on 25.11.2019
 */

public interface GetURIService {
    @POST("/BrokerService/json/GetURI")
    Call<GetURIResult> getURICall(@Body String instalationId);
}
