package md.intelectsoft.brokerservice;


import md.intelectsoft.brokerservice.body.InformationData;
import md.intelectsoft.brokerservice.body.SendGetURI;
import md.intelectsoft.brokerservice.body.SendRegisterApplication;
import md.intelectsoft.brokerservice.results.ErrorMessage;
import md.intelectsoft.brokerservice.results.GetNews;
import md.intelectsoft.brokerservice.results.RegisterApplication;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServiceAPI {
    @GET("/ISConnectionBrokerService/json/Ping")
    Call<Boolean> ping();

    @POST("/ISConnectionBrokerService/json/RegisterApplication")
    Call<RegisterApplication> registerApplicationCall(@Body SendRegisterApplication bodyRegisterApp);

    @POST("/ISConnectionBrokerService/json/GetURI")
    Call<RegisterApplication> getURICall(@Body SendGetURI sendGetURI);

    @POST("/ISConnectionBrokerService/json/UpdateDiagnosticInformation")
    Call<ErrorMessage> updateDiagnosticInfo(@Body InformationData informationData);

    @GET("/ISConnectionBrokerService/json/GetNews")
    Call<GetNews> getNews(@Query("ID") int id, @Query("ProductType") int productType);
}
