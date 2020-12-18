package md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote;

/**
 * Created by Igor on 25.11.2019
 */

public class ApiUtils {
    private static final String BaseURL_ConnectionBroker = "https://api.edi.md/";

    public static CommandServices commandBrokerService(){
        return RetrofitClient.getBrokerClient(BaseURL_ConnectionBroker).create(CommandServices.class);
    }
    public static CommandServices commandEposService (String uri){
        return RetrofitClient.getEposClient(uri).create(CommandServices.class);
    }
    public static CommandServices commandFPService (String uri){
        return RetrofitClient.getFpServiceClient("http://" + uri).create(CommandServices.class);
    }
}