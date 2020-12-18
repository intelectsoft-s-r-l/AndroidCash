package md.intelectsoft.salesepos.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class PaymentType extends RealmObject {

    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("ExternalId")
    @Expose
    private String externalId;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("PredefinedIndex")
    @Expose
    private Integer predefinedIndex;
    @SerializedName("PrintFiscalCheck")
    @Expose
    private Boolean printFiscalCheck;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPredefinedIndex() {
        return predefinedIndex;
    }

    public void setPredefinedIndex(Integer predefinedIndex) {
        this.predefinedIndex = predefinedIndex;
    }

    public Boolean getPrintFiscalCheck() {
        return printFiscalCheck;
    }

    public void setPrintFiscalCheck(Boolean printFiscalCheck) {
        this.printFiscalCheck = printFiscalCheck;
    }

}
