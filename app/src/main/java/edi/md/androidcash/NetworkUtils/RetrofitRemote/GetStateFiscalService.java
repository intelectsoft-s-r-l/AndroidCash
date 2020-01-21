package edi.md.androidcash.NetworkUtils.RetrofitRemote;

import edi.md.androidcash.NetworkUtils.FiscalServiceResult.SimpleResult;
import retrofit2.Call;
import retrofit2.http.GET;

public interface GetStateFiscalService {
    @GET("/fpservice/json/GetState")
    Call<SimpleResult> getState ();
}
