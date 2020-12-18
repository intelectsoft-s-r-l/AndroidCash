package md.intelectsoft.salesepos.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SaveBillLine {
    @SerializedName("Count")
    @Expose
    private Double count;
    @SerializedName("CreatedByID")
    @Expose
    private String createdByID;
    @SerializedName("CreationDate")
    @Expose
    private String creationDate;
    @SerializedName("DeletedByID")
    @Expose
    private String deletedByID;
    @SerializedName("DeletionDate")
    @Expose
    private String deletionDate;
    @SerializedName("IsDeleted")
    @Expose
    private Boolean isDeleted;
    @SerializedName("Price")
    @Expose
    private Double price;
    @SerializedName("PriceLineID")
    @Expose
    private String priceLineID;
    @SerializedName("PromoPrice")
    @Expose
    private Double promoPrice;
    @SerializedName("Sum")
    @Expose
    private Double sum;
    @SerializedName("SumWithDiscount")
    @Expose
    private Double sumWithDiscount;
    @SerializedName("VATQuote")
    @Expose
    private Double vATQuote;

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public String getCreatedByID() {
        return createdByID;
    }

    public void setCreatedByID(String createdByID) {
        this.createdByID = createdByID;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getDeletedByID() {
        return deletedByID;
    }

    public void setDeletedByID(String deletedByID) {
        this.deletedByID = deletedByID;
    }

    public String getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(String deletionDate) {
        this.deletionDate = deletionDate;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getPriceLineID() {
        return priceLineID;
    }

    public void setPriceLineID(String priceLineID) {
        this.priceLineID = priceLineID;
    }

    public Double getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(Double promoPrice) {
        this.promoPrice = promoPrice;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public Double getSumWithDiscount() {
        return sumWithDiscount;
    }

    public void setSumWithDiscount(Double sumWithDiscount) {
        this.sumWithDiscount = sumWithDiscount;
    }

    public Double getVATQuote() {
        return vATQuote;
    }

    public void setVATQuote(Double vATQuote) {
        this.vATQuote = vATQuote;
    }
}
