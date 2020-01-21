package edi.md.androidcash.NetworkUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by Igor on 19.11.2019
 */

public class Promotion extends RealmObject {
    @SerializedName("AllowDiscount")
    @Expose
    private Boolean allowDiscount;
    @SerializedName("EndDate")
    @Expose
    private String endDate;
    @SerializedName("ID")
    @Expose
    private String id;
    @SerializedName("Price")
    @Expose
    private Double price;
    @SerializedName("StartDate")
    @Expose
    private String startDate;
    @SerializedName("TimeBegin")
    @Expose
    private String timeBegin;
    @SerializedName("TimeEnd")
    @Expose
    private String timeEnd;

    public Boolean getAllowDiscount() {
        return allowDiscount;
    }

    public void setAllowDiscount(Boolean allowDiscount) {
        this.allowDiscount = allowDiscount;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getTimeBegin() {
        return timeBegin;
    }

    public void setTimeBegin(String timeBegin) {
        this.timeBegin = timeBegin;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }
}
