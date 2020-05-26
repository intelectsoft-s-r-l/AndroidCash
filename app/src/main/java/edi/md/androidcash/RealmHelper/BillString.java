package edi.md.androidcash.RealmHelper;
import androidx.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by Igor on 02.10.2019
 */

public class BillString extends RealmObject {
    @Required
    private String billID;
    @Required
    private String id;
    @Required
    private String assortmentExternID;
    private String assortmentFullName;
    private boolean allowNonInteger;
    private boolean allowDiscounts;
    @Required
    private String createBy;
    private long createDate;
    private String promoLineID;
    private String priceLineID;
    private double quantity;
    private double price;
    private double priceWithDiscount;
    private double promoPrice;
    private double sum;
    private double sumWithDiscount;
    private double vat;
    private String barcode;
    private boolean isDeleted;
    private long deletionDate;
    private String deleteBy;
    private boolean expanded;

    public String getBillID() {
        return billID;
    }

    public void setBillID(String billID) {
        this.billID = billID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssortmentExternID() {
        return assortmentExternID;
    }

    public void setAssortmentExternID(String assortmentExternID) {
        this.assortmentExternID = assortmentExternID;
    }

    public String getAssortmentFullName() {
        return assortmentFullName;
    }

    public void setAssortmentFullName(String assortmentFullName) {
        this.assortmentFullName = assortmentFullName;
    }

    public boolean isAllowNonInteger() {
        return allowNonInteger;
    }

    public void setAllowNonInteger(boolean allowNonInteger) {
        this.allowNonInteger = allowNonInteger;
    }

    public boolean isAllowDiscounts() {
        return allowDiscounts;
    }

    public void setAllowDiscounts(boolean allowDiscounts) {
        this.allowDiscounts = allowDiscounts;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public String getPromoLineID() {
        return promoLineID;
    }

    public void setPromoLineID(String promoLineID) {
        this.promoLineID = promoLineID;
    }

    public String getPriceLineID() {
        return priceLineID;
    }

    public void setPriceLineID(String priceLineID) {
        this.priceLineID = priceLineID;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceWithDiscount() {
        return priceWithDiscount;
    }

    public void setPriceWithDiscount(double priceWithDiscount) {
        this.priceWithDiscount = priceWithDiscount;
    }

    public double getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(double promoPrice) {
        this.promoPrice = promoPrice;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getSumWithDiscount() {
        return sumWithDiscount;
    }

    public void setSumWithDiscount(double sumWithDiscount) {
        this.sumWithDiscount = sumWithDiscount;
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public long getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(long deletionDate) {
        this.deletionDate = deletionDate;
    }

    public String getDeleteBy() {
        return deleteBy;
    }

    public void setDeleteBy(String deleteBy) {
        this.deleteBy = deleteBy;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
