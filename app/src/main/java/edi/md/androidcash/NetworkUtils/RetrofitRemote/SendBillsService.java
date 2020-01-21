package edi.md.androidcash.NetworkUtils.RetrofitRemote;

import edi.md.androidcash.NetworkUtils.EposResult.ResultEposSimple;
import edi.md.androidcash.NetworkUtils.SendBillsToServer;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Igor on 15.01.2020
 */

public interface SendBillsService {
    @POST("/epos/json/SaveBills")
    Call<ResultEposSimple> saveBillCall(@Body SendBillsToServer billsToServer);
}
