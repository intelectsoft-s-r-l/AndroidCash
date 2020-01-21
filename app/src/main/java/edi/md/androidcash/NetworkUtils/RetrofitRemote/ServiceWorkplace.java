package edi.md.androidcash.NetworkUtils.RetrofitRemote;


import edi.md.androidcash.NetworkUtils.EposResult.GetWorkPlaceService;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServiceWorkplace {
    @GET("/epos/json/GetWorkPlaces")
      Call<GetWorkPlaceService> getWorkplace(@Query("Token") String param1);
}
