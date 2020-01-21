package edi.md.androidcash.NetworkUtils.RetrofitRemote;


import edi.md.androidcash.NetworkUtils.EposResult.AssortmentListService;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetAssortmentListService {
    @GET("/epos/json/GetAssortmentList")
      Call<AssortmentListService> getAssortiment(@Query("Token") String param1, @Query("WorkplaceId") String param2);
}
