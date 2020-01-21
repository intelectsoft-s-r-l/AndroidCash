package edi.md.androidcash.NetworkUtils.RetrofitRemote;


import edi.md.androidcash.NetworkUtils.EposResult.UserListServiceResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetUserService {
    @GET("/epos/json/GetUsersList")
      Call<UserListServiceResult> getUsers(@Query("Token") String token, @Query("WorkplaceId") String workPlaceId);
}
