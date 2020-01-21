package edi.md.androidcash.NetworkUtils.RetrofitRemote;

import edi.md.androidcash.NetworkUtils.FiscalServiceResult.ZResponse;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Igor on 20.01.2020
 */

public interface PrintZService {
    @GET("/fpservice/json/PrintReportZ")
    Call<ZResponse> printZReport();
}
