package md.intelectsoft.salesepos.RealmHelper;
import io.realm.RealmObject;

/**
 * Created by Igor on 07.03.2020
 */

public class History extends RealmObject {
    private int type;
    private String msg;
    private long date;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
