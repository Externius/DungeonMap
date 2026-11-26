package externius.rdmg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Action(
    @SerialName("name") val name: String? = null,
    @SerialName("desc") val desc: String? = null,
    @SerialName("attack_bonus") val attackBonus: Int? = null,
    @SerialName("damage_dice") val damageDice: String? = null,
    @SerialName("damage_bonus") val damageBonus: Int? = null
)
