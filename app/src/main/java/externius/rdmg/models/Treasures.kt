package externius.rdmg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Treasures(
    @SerialName("name") val name: String? = null,
    @SerialName("cost") val cost: Int? = null,
    @SerialName("rarity") val rarity: Int? = null,
    @SerialName("magical") val magical: Boolean? = null,
    @SerialName("types") val types: List<String>? = null
)
