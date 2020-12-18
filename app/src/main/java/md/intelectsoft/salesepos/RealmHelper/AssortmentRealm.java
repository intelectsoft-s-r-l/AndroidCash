
package md.intelectsoft.salesepos.RealmHelper;

import md.intelectsoft.salesepos.NetworkUtils.Promotion;
import io.realm.RealmList;
import io.realm.RealmObject;

public class AssortmentRealm extends RealmObject {

    private String id;
    private String name;
    private String shortName;
    private boolean isFolder;
    private String parentID;
    private String marking;
    private String code;
    private RealmList<Barcodes> barcodes = null;
    private double price;
    private long priceLineStartDate;
    private long priceLineEndDate;
    private String priceLineId;
    private boolean enableSaleTimeRange;
    private boolean allowDiscounts;
    private boolean allowNonInteger;
    private RealmList<Promotion> promotions = null;
    private int QuickButtonNumber;
    private String quickGroupName;
    private long saleEndTime;
    private long saleStartTime;
    private double stockBalance;
    private double stockBalanceDate;
    private String unit;
    private double vat;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getMarking() {
        return marking;
    }

    public void setMarking(String marking) {
        this.marking = marking;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public RealmList<Barcodes> getBarcodes() {
        return barcodes;
    }

    public void setBarcodes(RealmList<Barcodes> barcodes) {
        this.barcodes = barcodes;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getPriceLineStartDate() {
        return priceLineStartDate;
    }

    public void setPriceLineStartDate(long priceLineStartDate) {
        this.priceLineStartDate = priceLineStartDate;
    }

    public long getPriceLineEndDate() {
        return priceLineEndDate;
    }

    public void setPriceLineEndDate(long priceLineEndDate) {
        this.priceLineEndDate = priceLineEndDate;
    }

    public String getPriceLineId() {
        return priceLineId;
    }

    public void setPriceLineId(String priceLineId) {
        this.priceLineId = priceLineId;
    }

    public boolean isEnableSaleTimeRange() {
        return enableSaleTimeRange;
    }

    public void setEnableSaleTimeRange(boolean enableSaleTimeRange) {
        this.enableSaleTimeRange = enableSaleTimeRange;
    }

    public boolean isAllowDiscounts() {
        return allowDiscounts;
    }

    public void setAllowDiscounts(boolean allowDiscounts) {
        this.allowDiscounts = allowDiscounts;
    }

    public boolean isAllowNonInteger() {
        return allowNonInteger;
    }

    public void setAllowNonInteger(boolean allowNonInteger) {
        this.allowNonInteger = allowNonInteger;
    }

    public RealmList<Promotion> getPromotions() {
        return promotions;
    }

    public void setPromotions(RealmList<Promotion> promotions) {
        this.promotions = promotions;
    }

    public int getQuickButtonNumber() {
        return QuickButtonNumber;
    }

    public void setQuickButtonNumber(int quickButtonNumber) {
        QuickButtonNumber = quickButtonNumber;
    }

    public String getQuickGroupName() {
        return quickGroupName;
    }

    public void setQuickGroupName(String quickGroupName) {
        this.quickGroupName = quickGroupName;
    }

    public long getSaleEndTime() {
        return saleEndTime;
    }

    public void setSaleEndTime(long saleEndTime) {
        this.saleEndTime = saleEndTime;
    }

    public long getSaleStartTime() {
        return saleStartTime;
    }

    public void setSaleStartTime(long saleStartTime) {
        this.saleStartTime = saleStartTime;
    }

    public double getStockBalance() {
        return stockBalance;
    }

    public void setStockBalance(double stockBalance) {
        this.stockBalance = stockBalance;
    }

    public double getStockBalanceDate() {
        return stockBalanceDate;
    }

    public void setStockBalanceDate(double stockBalanceDate) {
        this.stockBalanceDate = stockBalanceDate;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }
}
