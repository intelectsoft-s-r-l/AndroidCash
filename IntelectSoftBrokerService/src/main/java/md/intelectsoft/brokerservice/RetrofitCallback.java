package md.intelectsoft.brokerservice;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitCallback<T> implements Callback<T>{

    @Override
    public void onResponse(Call call, Response response) {

    }

    @Override
    public void onFailure(Call call, Throwable t) {

    }
}
