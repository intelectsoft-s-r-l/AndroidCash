package edi.md.androidcash.NetworkUtils.RetrofitRemote;
import edi.md.androidcash.FiscalService.PrintBillFiscalService;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.Body.BodyDeactivateApp;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.Body.BodyRegisterApp;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.Body.BodyUnRegisterApp;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.DeactivateApplicationResult;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.GetURIResult;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.RegisterApplicationResult;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.UnRegisterApplicationResult;
import edi.md.androidcash.NetworkUtils.EposResult.AssortmentListService;
import edi.md.androidcash.NetworkUtils.EposResult.AuthentificateUserResult;
import edi.md.androidcash.NetworkUtils.EposResult.GetWorkPlaceService;
import edi.md.androidcash.NetworkUtils.EposResult.ResultEposSimple;
import edi.md.androidcash.NetworkUtils.EposResult.UserListServiceResult;
import edi.md.androidcash.NetworkUtils.EposResult.WorkPlaceSettings;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.SimpleResult;
import edi.md.androidcash.NetworkUtils.SendBillsToServer;
import edi.md.androidcash.NetworkUtils.SendShiftToServer;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Igor on 31.01.2020
 */

public interface CommandServices {
    @POST("/brokerservice/json/DeactivateApplication")
    Call<DeactivateApplicationResult> deactivateApplicationCall(@Body BodyDeactivateApp bodyDeactivateApp);

    @POST("/brokerservice/json/RegisterApplication")
    Call<RegisterApplicationResult> registerApplicationCall(@Body BodyRegisterApp bodyRegisterApp);

    @POST("/brokerservice/json/GetURI")
    Call<GetURIResult> getURICall(@Body String instalationId);

    @POST("/brokerservice/json/UnRegisterApplication")
    Call<UnRegisterApplicationResult> unRegisterApplicationCall(@Body BodyUnRegisterApp bodyUnRegisterApp);

    @POST("/fpservice/json/PrintBill")
    Call<SimpleResult> printBill (@Body PrintBillFiscalService bill);

    @GET("/fpservice/json/PrintReportX")
    Call<SimpleResult> printXReport (@Query("prn") int param);

    @GET("/fpservice/json/PrintReportZ")
    Call<SimpleResult> printZReport(@Query("prn") int param);

    @GET("/fpservice/json/GetState")
    Call<SimpleResult> getState ();

    @GET("/epos/json/GetAssortmentList")
    Call<AssortmentListService> getAssortiment(@Query("Token") String param1, @Query("WorkplaceId") String param2);

    @POST("/epos/json/SaveBills")
    Call<ResultEposSimple> saveBillCall(@Body SendBillsToServer billsToServer);

    @POST("/epos/json/SaveShift")
    Call<ResultEposSimple> saveShiftCall(@Body SendShiftToServer shiftToServer);

    @GET("/epos/json/GetWorkPlaces")
    Call<GetWorkPlaceService> getWorkplace(@Query("Token") String param1);

    @GET("/epos/json/GetWorkplaceSettings")
    Call<WorkPlaceSettings> getWorkplaceSettings(@Query("Token") String token, @Query("WorkplaceId") String workPlaceId);

    @GET("/epos/json/AuthentificateUser")
    Call<AuthentificateUserResult> autentificateUser (@Query("APIKey") String apiKey, @Query("userLogin") String userLogin, @Query("userPass") String userPass);

    @GET("/epos/json/GetUsersList")
    Call<UserListServiceResult> getUsers(@Query("Token") String token, @Query("WorkplaceId") String workPlaceId);


}
