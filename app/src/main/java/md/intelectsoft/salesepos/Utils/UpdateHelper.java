package md.intelectsoft.salesepos.Utils;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Igor on 12.05.2020
 */

public class UpdateHelper {
    public static String KEY_UPDATE_URL = "update_url";
    public static String KEY_UPDATE_VERSION = "version";
    public static String KEY_UPDATE_ENABLE = "is_update";
    public static String KEY_UPDATE_CHANGES = "changes";

    public static String KEY_UPDATE_TRIAL_CHANGES = "changes_trial";
    public static String KEY_UPDATE_TRIAL_ENABLE = "is_update_trial";
    public static String KEY_UPDATE_TRIAL_VERSION = "version_trial";
    public static String KEY_UPDATE_TRIAL_URL = "update_url_trial";


    public interface OnUpdateCheckListener{
        void onUpdateCheckListener(UpdateInformation information);
    }

    public static Builder with(Context context){
        return new Builder(context);
    }

    private OnUpdateCheckListener onUpdateCheckListener;
    private Context context;

    public UpdateHelper(Context context,OnUpdateCheckListener onUpdateCheckListener) {
        this.onUpdateCheckListener = onUpdateCheckListener;
        this.context = context;
    }

    public void check(){
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        UpdateInformation updateInformation = new UpdateInformation();

        updateInformation.setUpdate(remoteConfig.getBoolean(KEY_UPDATE_ENABLE));

        //changes convert to class
        String changes = remoteConfig.getString(KEY_UPDATE_CHANGES);

        Gson gson = new Gson();
        UpdateChanges changes1 = gson.fromJson(changes,UpdateChanges.class);

        updateInformation.setChanges(changes1);
        updateInformation.setNewVerion(remoteConfig.getString(KEY_UPDATE_VERSION));
        updateInformation.setUrl(remoteConfig.getString(KEY_UPDATE_URL));

        updateInformation.setUpdateTrial(remoteConfig.getBoolean(KEY_UPDATE_TRIAL_ENABLE));
        //trial changes convert to class
        String changes_trial = remoteConfig.getString(KEY_UPDATE_TRIAL_CHANGES);
        UpdateChanges changes2 = gson.fromJson(changes_trial,UpdateChanges.class);

        updateInformation.setChangesTrial(changes2);
        updateInformation.setNewVersionTrial(remoteConfig.getString(KEY_UPDATE_TRIAL_VERSION));
        updateInformation.setUrlTrial(remoteConfig.getString(KEY_UPDATE_TRIAL_URL));

        updateInformation.setCurrentVersion(getAppVersion(context));

        onUpdateCheckListener.onUpdateCheckListener(updateInformation);

    }

    private String getAppVersion(Context context){
        String result = "";

        try{
            result = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
            result = result.replaceAll("[a-zA-Z] |-","");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class Builder{

        private Context context;
        private OnUpdateCheckListener onUpdateCheckListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateCheck(OnUpdateCheckListener onUpdateCheckListener){
            this.onUpdateCheckListener = onUpdateCheckListener;
            return this;
        }

        public UpdateHelper build(){
            return new UpdateHelper(context,onUpdateCheckListener);
        }

        public UpdateHelper check(){
            UpdateHelper updateHelper = build();
            updateHelper.check();

            return updateHelper;
        }
    }
}
