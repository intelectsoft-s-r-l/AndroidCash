package md.intelectsoft.brokerservice.results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class NewsList {

    @SerializedName("AppType")
    @Expose
    private Integer appType;
    @SerializedName("CompanyID")
    @Expose
    private Integer companyID;
    @SerializedName("CompanyLogo")
    @Expose
    private String companyLogo;
    @SerializedName("Content")
    @Expose
    private String content;
    @SerializedName("CreateDate")
    @Expose
    private String createDate;
    @SerializedName("Header")
    @Expose
    private String header;
    @SerializedName("ID")
    @Expose
    private Integer iD;
    @SerializedName("Photo")
    @Expose
    private String photo;
    @SerializedName("Status")
    @Expose
    private Integer status;

    private long dateLong;

    private String internID;

    public Integer getAppType() {
        return appType;
    }

    public void setAppType(Integer appType) {
        this.appType = appType;
    }

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Integer getID() {
        return iD;
    }

    public void setID(Integer iD) {
        this.iD = iD;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public long getDateLong() {
        return dateLong;
    }

    public void setDateLong(long dateLong) {
        this.dateLong = dateLong;
    }

    public String getInternID() {
        return internID;
    }

    public void setInternID(String internID) {
        this.internID = internID;
    }
}
