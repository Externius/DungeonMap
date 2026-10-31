package externius.rdmg.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Treasures {
    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("cost")
    @Expose
    var cost: Int? = null

    @SerializedName("rarity")
    @Expose
    var rarity: Int? = null

    @SerializedName("magical")
    @Expose
    var magical: Boolean? = null

    @SerializedName("types")
    @Expose
    var types: List<String>? = null
}
