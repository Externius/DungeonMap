package externius.rdmg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Monster(
    @SerialName("name") val name: String? = null,
    @SerialName("size") val size: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("subtype") val subtype: String? = null,
    @SerialName("alignment") val alignment: String? = null,
    @SerialName("armor_class") val armorClass: Int? = null,
    @SerialName("hit_points") val hitPoints: Int? = null,
    @SerialName("hit_dice") val hitDice: String? = null,
    @SerialName("speed") val speed: String? = null,
    @SerialName("strength") val strength: Int? = null,
    @SerialName("dexterity") val dexterity: Int? = null,
    @SerialName("constitution") val constitution: Int? = null,
    @SerialName("intelligence") val intelligence: Int? = null,
    @SerialName("wisdom") val wisdom: Int? = null,
    @SerialName("charisma") val charisma: Int? = null,
    @SerialName("constitution_save") val constitutionSave: Int? = null,
    @SerialName("intelligence_save") val intelligenceSave: Int? = null,
    @SerialName("wisdom_save") val wisdomSave: Int? = null,
    @SerialName("history") val history: Int? = null,
    @SerialName("perception") val perception: Int? = null,
    @SerialName("damage_vulnerabilities") val damageVulnerabilities: String? = null,
    @SerialName("damage_resistances") val damageResistances: String? = null,
    @SerialName("damage_immunities") val damageImmunities: String? = null,
    @SerialName("condition_immunities") val conditionImmunities: String? = null,
    @SerialName("senses") val senses: String? = null,
    @SerialName("languages") val languages: String? = null,
    @SerialName("challenge_rating") val challengeRating: String? = null,
    @SerialName("special_abilities") val specialAbilities: List<SpecialAbility>? = null,
    @SerialName("actions") val actions: List<Action>? = null,
    @SerialName("legendary_actions") val legendaryActions: List<LegendaryAction>? = null
)
