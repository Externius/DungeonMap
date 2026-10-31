package externius.rdmg.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Action {
    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("desc")
    @Expose
    var desc: String? = null

    @SerializedName("attack_bonus")
    @Expose
    var attackBonus: Int? = null

    @SerializedName("damage_dice")
    @Expose
    var damageDice: String? = null

    @SerializedName("damage_bonus")
    @Expose
    var damageBonus: Int? = null
}
