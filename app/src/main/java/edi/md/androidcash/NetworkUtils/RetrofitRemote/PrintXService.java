package edi.md.androidcash.NetworkUtils.RetrofitRemote;

import edi.md.androidcash.NetworkUtils.FiscalServiceResult.XResponse;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Igor on 18.01.2020
 */

public interface PrintXService {
    @GET("/fpservice/json/PrintReportX")
    Call<XResponse> printXReport ();
}
