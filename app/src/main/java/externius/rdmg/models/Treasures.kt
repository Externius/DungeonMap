package externius.rdmg.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Treasures {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("cost")
    @Expose
    private Integer cost;
    @SerializedName("rarity")
    @Expose
    private Integer rarity;
    @SerializedName("magical")
    @Expose
    private Boolean magical;
    @SerializedName("types")
    @Expose
    private List<String> types = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Integer getRarity() {
        return rarity;
    }

    public void setRarity(Integer rarity) {
        this.rarity = rarity;
    }


    public Boolean getMagical() {
        return magical;
    }

    public void setMagical(Boolean magical) {
        this.magical = magical;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

}