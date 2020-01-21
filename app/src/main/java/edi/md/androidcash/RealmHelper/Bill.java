package edi.md.androidcash.RealmHelper;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Igor on 02.10.2019
 */

public class Bill extends RealmObject {
    private String id;
    private String shiftId;
    private int shiftReceiptNumSoftware;
    private int receiptNumFiscalMemory;
    private String discountCardId;
    private String discountCardNumber;
    private String deviceId;
    private long createDate;
    private long closeDate;
    private int state;  // 0 - opened , 1 - closed , 2 - deleted
    private String currentSoftwareVersion;
    private String author;   //user id
    private double sumWithDiscount;
    private double sum;
    private boolean isSinchronized;
    private RealmList<BillString> billStrings;
    private RealmList<BillPaymentType> billPaymentTypes;
    private String closedBy;
    private String lastEditAuthor;
    private long lastEditDate;
    private int inProcessOfSync; //0 - este finisat cu succes , 1- e in proces , 2 - nu este in proces

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public int getShiftReceiptNumSoftware() {
        return shiftReceiptNumSoftware;
    }

    public void setShiftReceiptNumSoftware(int shiftReceiptNumSoftware) {
        this.shiftReceiptNumSoftware = shiftReceiptNumSoftware;
    }

    public int getReceiptNumFiscalMemory() {
        return receiptNumFiscalMemory;
    }

    public void setReceiptNumFiscalMemory(int receiptNumFiscalMemory) {
        this.receiptNumFiscalMemory = receiptNumFiscalMemory;
    }

    public String getDiscountCardId() {
        return discountCardId;
    }

    public void setDiscountCardId(String discountCardId) {
        this.discountCardId = discountCardId;
    }

    public String getDiscountCardNumber() {
        return discountCardNumber;
    }

    public void setDiscountCardNumber(String discountCardNumber) {
        this.discountCardNumber = discountCardNumber;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(long closeDate) {
        this.closeDate = closeDate;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getCurrentSoftwareVersion() {
        return currentSoftwareVersion;
    }

    public void setCurrentSoftwareVersion(String currentSoftwareVersion) {
        this.currentSoftwareVersion = currentSoftwareVersion;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getSumWithDiscount() {
        return sumWithDiscount;
    }

    public void setSumWithDiscount(double sumWithDiscount) {
        this.sumWithDiscount = sumWithDiscount;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public boolean isSinchronized() {
        return isSinchronized;
    }

    public void setSinchronized(boolean sinchronized) {
        isSinchronized = sinchronized;
    }

    public RealmList<BillString> getBillStrings() {
        return billStrings;
    }

    public void setBillStrings(RealmList<BillString> billStrings) {
        this.billStrings = billStrings;
    }

    public RealmList<BillPaymentType> getBillPaymentTypes() {
        return billPaymentTypes;
    }

    public void setBillPaymentTypes(RealmList<BillPaymentType> billPaymentTypes) {
        this.billPaymentTypes = billPaymentTypes;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public String getLastEditAuthor() {
        return lastEditAuthor;
    }

    public void setLastEditAuthor(String lastEditAuthor) {
        this.lastEditAuthor = lastEditAuthor;
    }

    public long getLastEditDate() {
        return lastEditDate;
    }

    public void setLastEditDate(long lastEditDate) {
        this.lastEditDate = lastEditDate;
    }

    public int isInProcessOfSync() {
        return inProcessOfSync;
    }

    public void setInProcessOfSync(int inProcessOfSync) {
        this.inProcessOfSync = inProcessOfSync;
    }
}
