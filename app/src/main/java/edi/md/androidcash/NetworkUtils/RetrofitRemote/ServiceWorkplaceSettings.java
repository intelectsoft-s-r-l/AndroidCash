package edi.md.androidcash.NetworkUtils.RetrofitRemote;


import edi.md.androidcash.NetworkUtils.EposResult.WorkPlaceSettings;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServiceWorkplaceSettings {
    @GET("/epos/json/GetWorkplaceSettings")
      Call<WorkPlaceSettings> getWorkplaceSettings(@Query("Token") String token, @Query("WorkplaceId") String workPlaceId);
}
