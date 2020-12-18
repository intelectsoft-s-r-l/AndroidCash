package md.intelectsoft.brokerservice;

import com.sun.org.apache.xpath.internal.operations.Bool;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Broker implements BrokerInterface {

    private ServiceAPI serviceAPI = RetrofitClient.getApiBrokerService();

    @Override
    public boolean onPing() {
        return false;
    }
}