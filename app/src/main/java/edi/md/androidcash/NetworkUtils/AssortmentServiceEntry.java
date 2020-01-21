package edi.md.androidcash.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AssortmentServiceEntry {

    @SerializedName("AllowDiscounts")
    @Expose
    private boolean allowDiscounts;
    @SerializedName("AllowNonInteger")
    @Expose
    private boolean allowNonInteger;
    @SerializedName("Barcodes")
    @Expose
    private List<String> barcodes = null;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("EnableSaleTimeRange")
    @Expose
    private boolean enableSaleTimeRange;
    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("IsFolder")
    @Expose
    private boolean isFolder;
    @SerializedName("Marking")
    @Expose
    private String marking;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("ParentID")
    @Expose
    private String parentID;
    @SerializedName("Price")
    @Expose
    private double price;
    @SerializedName("PriceLineEndDate")
    @Expose
    private String priceLineEndDate;
    @SerializedName("PriceLineId")
    @Expose
    private String priceLineId;
    @SerializedName("PriceLineStartDate")
    @Expose
    private String priceLineStartDate;
    @SerializedName("Promotions")
    @Expose
    private List<Promotion> promotions = null;
    @SerializedName("QuickButtonNumber")
    @Expose
    private int QuickButtonNumber;
    @SerializedName("QuickGroupName")
    @Expose
    private String quickGroupName;
    @SerializedName("SaleEndTime")
    @Expose
    private String saleEndTime;
    @SerializedName("SaleStartTime")
    @Expose
    private String saleStartTime;
    @SerializedName("ShortName")
    @Expose
    private String shortName;
    @SerializedName("StockBalance")
    @Expose
    private double stockBalance;
    @SerializedName("StockBalanceDate")
    @Expose
    private double stockBalanceDate;
    @SerializedName("Unit")
    @Expose
    private String unit;
    @SerializedName("VAT")
    @Expose
    private double vAT;


    public List<Promotion> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    public boolean getAllowDiscounts() {
        return allowDiscounts;
    }

    public void setAllowDiscounts(boolean allowDiscounts) {
        this.allowDiscounts = allowDiscounts;
    }

    public boolean getAllowNonInteger() {
        return allowNonInteger;
    }

    public void setAllowNonInteger(boolean allowNonInteger) {
        this.allowNonInteger = allowNonInteger;
    }

    public List<String> getBarcodes() {
        return barcodes;
    }

    public void setBarcodes(List<String> barcodes) {
        this.barcodes = barcodes;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean getEnableSaleTimeRange() {
        return enableSaleTimeRange;
    }

    public void setEnableSaleTimeRange(boolean enableSaleTimeRange) {
        this.enableSaleTimeRange = enableSaleTimeRange;
    }

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public boolean getIsFolder() {
        return isFolder;
    }

    public void setIsFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public int getQuickButtonNumber() {
        return QuickButtonNumber;
    }

    public void setQuickButtonNumber(int isQuickButton) {
        this.QuickButtonNumber = isQuickButton;
    }

    public String getMarking() {
        return marking;
    }

    public void setMarking(String marking) {
        this.marking = marking;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPriceLineEndDate() {
        return priceLineEndDate;
    }

    public void setPriceLineEndDate(String priceLineEndDate) {
        this.priceLineEndDate = priceLineEndDate;
    }

    public String getPriceLineId() {
        return priceLineId;
    }

    public void setPriceLineId(String priceLineId) {
        this.priceLineId = priceLineId;
    }

    public String getPriceLineStartDate() {
        return priceLineStartDate;
    }

    public void setPriceLineStartDate(String priceLineStartDate) {
        this.priceLineStartDate = priceLineStartDate;
    }

    public String getQuickGroupName() {
        return quickGroupName;
    }

    public void setQuickGroupName(String quickGroupName) {
        this.quickGroupName = quickGroupName;
    }

    public String getSaleEndTime() {
        return saleEndTime;
    }

    public void setSaleEndTime(String saleEndTime) {
        this.saleEndTime = saleEndTime;
    }

    public String getSaleStartTime() {
        return saleStartTime;
    }

    public void setSaleStartTime(String saleStartTime) {
        this.saleStartTime = saleStartTime;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
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

    public double getVAT() {
        return vAT;
    }

    public void setVAT(double vAT) {
        this.vAT = vAT;
    }

}
