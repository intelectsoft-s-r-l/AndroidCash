package md.intelectsoft.salesepos.Utils;
/**
 * Created by Igor on 13.05.2020
 */

public class UpdateInformation {
    private boolean isUpdate;
    private String newVerion;
    private String url;
    private UpdateChanges changes;

    private boolean isUpdateTrial;
    private String newVersionTrial;
    private String urlTrial;
    private UpdateChanges changesTrial;

    private String currentVersion;

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public String getNewVerion() {
        return newVerion;
    }

    public void setNewVerion(String newVerion) {
        this.newVerion = newVerion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UpdateChanges getChanges() {
        return changes;
    }

    public void setChanges(UpdateChanges changes) {
        this.changes = changes;
    }

    public boolean isUpdateTrial() {
        return isUpdateTrial;
    }

    public void setUpdateTrial(boolean updateTrial) {
        isUpdateTrial = updateTrial;
    }

    public String getNewVersionTrial() {
        return newVersionTrial;
    }

    public void setNewVersionTrial(String newVersionTrial) {
        this.newVersionTrial = newVersionTrial;
    }

    public String getUrlTrial() {
        return urlTrial;
    }

    public void setUrlTrial(String urlTrial) {
        this.urlTrial = urlTrial;
    }

    public UpdateChanges getChangesTrial() {
        return changesTrial;
    }

    public void setChangesTrial(UpdateChanges changesTrial) {
        this.changesTrial = changesTrial;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }
}
