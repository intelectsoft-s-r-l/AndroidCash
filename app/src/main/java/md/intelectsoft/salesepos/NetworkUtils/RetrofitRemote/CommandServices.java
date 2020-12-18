package md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote;

import md.intelectsoft.salesepos.FiscalService.PrintBillFiscalService;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.AssortmentListService;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.AuthentificateUserResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.GetWorkPlaceService;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.ResultEposSimple;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.UserListServiceResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.WorkPlaceSettings;
import md.intelectsoft.salesepos.NetworkUtils.FiscalServiceResult.SimpleResult;
import md.intelectsoft.salesepos.NetworkUtils.SendBillsToServer;
import md.intelectsoft.salesepos.NetworkUtils.SendShiftToServer;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Igor on 31.01.2020
 */

public interface CommandServices {

    @POST("/fpservice/json/PrintBill")
    Call<SimpleResult> printBill (@Body PrintBillFiscalService bill);

    @GET("/fpservice/json/PrintReportX")
    Call<SimpleResult> printXReport (@Query("prn") int param);

    @GET("/fpservice/json/PrintReportZ")
    Call<SimpleResult> printZReport(@Query("prn") int param);

    @GET("/fpservice/json/GetState")
    Call<SimpleResult> getState ();



    @GET("json/GetAssortmentList")
    Call<AssortmentListService> getAssortiment(@Query("Token") String param1, @Query("WorkplaceId") String param2);

    @POST("json/SaveBills")
    Call<ResultEposSimple> saveBillCall(@Body SendBillsToServer billsToServer);

    @POST("json/SaveShift")
    Call<ResultEposSimple> saveShiftCall(@Body SendShiftToServer shiftToServer);

    @GET("json/GetWorkPlaces")
    Call<GetWorkPlaceService> getWorkplace(@Query("Token") String param1);

    @GET("json/GetWorkplaceSettings")
    Call<WorkPlaceSettings> getWorkplaceSettings(@Query("Token") String token, @Query("WorkplaceId") String workPlaceId);

    @GET("json/AuthentificateUser")
    Call<AuthentificateUserResult> autentificateUser (@Query("APIKey") String apiKey, @Query("userLogin") String userLogin, @Query("userPass") String userPass);

    @GET("json/GetUsersList")
    Call<UserListServiceResult> getUsers(@Query("Token") String token, @Query("WorkplaceId") String workPlaceId);


}
