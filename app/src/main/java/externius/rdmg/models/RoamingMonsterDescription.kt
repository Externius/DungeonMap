package externius.rdmg.models

import kotlinx.serialization.Serializable

@Serializable
data class RoamingMonsterDescription(
    val name: String? = null,
    val description: String? = null
)
