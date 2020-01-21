package edi.md.androidcash.NetworkUtils.RetrofitRemote;

import edi.md.androidcash.FiscalService.PrintBillFiscalService;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.SimpleResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Igor on 20.01.2020
 */

public interface PrintBillService {
    @POST("/fpservice/json/PrintBill")
    Call<SimpleResult> printBill (@Body PrintBillFiscalService bill);
}
