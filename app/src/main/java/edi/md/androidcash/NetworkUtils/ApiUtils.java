package edi.md.androidcash.NetworkUtils;

import android.content.Context;

import edi.md.androidcash.NetworkUtils.RetrofitRemote.DeactivateAppService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.GetAssortmentListService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.GetStateFiscalService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.GetURIService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.GetUserService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.PrintBillService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.PrintXService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.PrintZService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.RegisterAppService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.RetrofitClient;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.SendBillsService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.SendShiftService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ServiceWorkplaceSettings;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.UnRegisterAppService;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Igor on 25.11.2019
 */

public class ApiUtils {
    private static final String BaseURL_ConnectionBroker = "https://cb.edi.md:5050";

    public static RegisterAppService registerAppService(){
        return RetrofitClient.getBrokerClient(BaseURL_ConnectionBroker).create(RegisterAppService.class);
    }

    public static GetURIService getURIService(){
        return RetrofitClient.getBrokerClient(BaseURL_ConnectionBroker).create(GetURIService.class);
    }
    public static UnRegisterAppService unRegisterAppService (){
        return RetrofitClient.getBrokerClient(BaseURL_ConnectionBroker).create(UnRegisterAppService.class);
    }
    public static DeactivateAppService deactivateAppService(){
        return RetrofitClient.getBrokerClient(BaseURL_ConnectionBroker).create(DeactivateAppService.class);
    }
    public static ServiceWorkplaceSettings workplaceSettingsService (Context context){
        String uri = "http://" + context.getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);
        return RetrofitClient.getEposClient(uri).create(ServiceWorkplaceSettings.class);
    }
    public static GetUserService getUserService (Context context){
        String uri = "http://" + context.getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);
        return RetrofitClient.getEposClient(uri).create(GetUserService.class);
    }
    public static GetAssortmentListService getAssortmentListService (Context context){
        String uri = "http://" + context.getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);
        return RetrofitClient.getEposClient(uri).create(GetAssortmentListService.class);
    }
    public static SendShiftService getSaveShiftService (String uri){
        return RetrofitClient.getEposClient("http://" + uri).create(SendShiftService.class);
    }
    public static SendBillsService getSaveBillService (String uri){
        return RetrofitClient.getEposClient("http://" + uri).create(SendBillsService.class);
    }
    public static PrintXService printXService (String uri){
        return RetrofitClient.getFpServiceClient("http://" + uri).create(PrintXService.class);
    }
    public static PrintZService printZService (String uri){
        return RetrofitClient.getFpServiceClient("http://" + uri).create(PrintZService.class);
    }
    public static PrintBillService printBillService (String uri){
        return RetrofitClient.getFpServiceClient("http://" + uri).create(PrintBillService.class);
    }
    public static GetStateFiscalService getStateFiscalService (String uri){
        return RetrofitClient.getFpServiceClient("http://" + uri).create(GetStateFiscalService.class);
    }
}