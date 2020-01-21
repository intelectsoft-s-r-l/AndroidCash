package edi.md.androidcash.NetworkUtils.RetrofitRemote;

import edi.md.androidcash.NetworkUtils.EposResult.ResultEposSimple;
import edi.md.androidcash.NetworkUtils.SendShiftToServer;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Igor on 15.01.2020
 */

public interface SendShiftService {
    @POST("/epos/json/SaveShift")
    Call<ResultEposSimple> saveShiftCall(@Body SendShiftToServer shiftToServer);
}
