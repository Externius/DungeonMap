package externius.rdmg.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SpecialAbility {
    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("desc")
    @Expose
    var desc: String? = null

    @SerializedName("attack_bonus")
    @Expose
    var attackBonus: Int? = null
}
