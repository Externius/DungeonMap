package externius.rdmg.models

import kotlinx.serialization.Serializable

@Serializable
data class RoomDescription(
    val name: String? = null,
    val treasure: String? = null,
    val monster: String? = null,
    val doors: String? = null
)
