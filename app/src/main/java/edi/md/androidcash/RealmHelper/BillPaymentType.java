package edi.md.androidcash.RealmHelper;
import io.realm.RealmObject;

/**
 * Created by Igor on 31.10.2019
 */

public class BillPaymentType extends RealmObject {
    private String Id;
    private String billID;
    private String Author;
    private long CreateDate;
    private String PaymentTypeID;
    private String Name;
    private int PaymentCode; // 404 - кода нет
    private double Sum;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getBillID() {
        return billID;
    }

    public void setBillID(String billID) {
        this.billID = billID;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public long getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(long createDate) {
        CreateDate = createDate;
    }

    public String getPaymentTypeID() {
        return PaymentTypeID;
    }

    public void setPaymentTypeID (String paymentTypeID) {
        PaymentTypeID = paymentTypeID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getPaymentCode() {
        return PaymentCode;
    }

    public void setPaymentCode(int paymentCode) {
        PaymentCode = paymentCode;
    }

    public double getSum() {
        return Sum;
    }

    public void setSum(double sum) {
        Sum = sum;
    }
}
