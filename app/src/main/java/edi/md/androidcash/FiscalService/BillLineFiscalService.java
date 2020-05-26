package edi.md.androidcash.FiscalService;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BillLineFiscalService {
    @SerializedName("Amount")
    @Expose
    private Double amount;
    @SerializedName("Discount")
    @Expose
    private double discount;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("PLU")
    @Expose
    private Integer pLU;
    @SerializedName("Price")
    @Expose
    private Double price;
    @SerializedName("VAT")
    @Expose
    private String vAT;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPLU() {
        return pLU;
    }

    public void setPLU(Integer pLU) {
        this.pLU = pLU;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getVAT() {
        return vAT;
    }

    public void setVAT(String vAT) {
        this.vAT = vAT;
    }

}
