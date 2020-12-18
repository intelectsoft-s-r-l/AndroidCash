package md.intelectsoft.salesepos.RealmHelper;
import io.realm.RealmObject;

/**
 * Created by Igor on 23.12.2019
 */

public class Barcodes extends RealmObject {
    private String bar;

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }
}
