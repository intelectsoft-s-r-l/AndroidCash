package edi.md.androidcash.RealmHelper;
/**
 * Created by Igor on 08.11.2019
 */

public class ShiftHistory {
    private String Uid;
    private String ShiftId;
    private long CreateDate;
    private String Author;
    private String Action;
    private String Sum;

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getShiftId() {
        return ShiftId;
    }

    public void setShiftId(String shiftId) {
        ShiftId = shiftId;
    }

    public long getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(long createDate) {
        CreateDate = createDate;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getAction() {
        return Action;
    }

    public void setAction(String action) {
        Action = action;
    }

    public String getSum() {
        return Sum;
    }

    public void setSum(String sum) {
        Sum = sum;
    }
}


