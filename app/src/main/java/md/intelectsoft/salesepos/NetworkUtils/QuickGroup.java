package md.intelectsoft.salesepos.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuickGroup {
    @SerializedName("AssortmentID")
    @Expose
    private List<String> assortmentID = null;
    @SerializedName("Name")
    @Expose
    private String name;

    public List<String> getAssortmentID() {
        return assortmentID;
    }

    public void setAssortmentID(List<String> assortmentID) {
        this.assortmentID = assortmentID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
