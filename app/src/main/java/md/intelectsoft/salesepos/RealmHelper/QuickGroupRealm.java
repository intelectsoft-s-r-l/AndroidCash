package md.intelectsoft.salesepos.RealmHelper;

import io.realm.RealmList;
import io.realm.RealmObject;

public class QuickGroupRealm extends RealmObject {
    private String groupName;
    private RealmList<String> assortmentId;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public RealmList<String> getAssortmentId() {
        return assortmentId;
    }

    public void setAssortmentId(RealmList<String> assortmentId) {
        this.assortmentId = assortmentId;
    }
}
