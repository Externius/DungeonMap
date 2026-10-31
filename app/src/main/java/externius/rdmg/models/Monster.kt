package externius.rdmg.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Monster {
    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("size")
    @Expose
    var size: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("subtype")
    @Expose
    var subtype: String? = null

    @SerializedName("alignment")
    @Expose
    var alignment: String? = null

    @SerializedName("armor_class")
    @Expose
    var armorClass: Int? = null

    @SerializedName("hit_points")
    @Expose
    var hitPoints: Int? = null

    @SerializedName("hit_dice")
    @Expose
    var hitDice: String? = null

    @SerializedName("speed")
    @Expose
    var speed: String? = null

    @SerializedName("strength")
    @Expose
    var strength: Int? = null

    @SerializedName("dexterity")
    @Expose
    var dexterity: Int? = null

    @SerializedName("constitution")
    @Expose
    var constitution: Int? = null

    @SerializedName("intelligence")
    @Expose
    var intelligence: Int? = null

    @SerializedName("wisdom")
    @Expose
    var wisdom: Int? = null

    @SerializedName("charisma")
    @Expose
    var charisma: Int? = null

    @SerializedName("constitution_save")
    @Expose
    var constitutionSave: Int? = null

    @SerializedName("intelligence_save")
    @Expose
    var intelligenceSave: Int? = null

    @SerializedName("wisdom_save")
    @Expose
    var wisdomSave: Int? = null

    @SerializedName("history")
    @Expose
    var history: Int? = null

    @SerializedName("perception")
    @Expose
    var perception: Int? = null

    @SerializedName("damage_vulnerabilities")
    @Expose
    var damageVulnerabilities: String? = null

    @SerializedName("damage_resistances")
    @Expose
    var damageResistances: String? = null

    @SerializedName("damage_immunities")
    @Expose
    var damageImmunities: String? = null

    @SerializedName("condition_immunities")
    @Expose
    var conditionImmunities: String? = null

    @SerializedName("senses")
    @Expose
    var senses: String? = null

    @SerializedName("languages")
    @Expose
    var languages: String? = null

    @SerializedName("challenge_rating")
    @Expose
    var challengeRating: String? = null

    @SerializedName("special_abilities")
    @Expose
    var specialAbilities: List<SpecialAbility>? = null

    @SerializedName("actions")
    @Expose
    var actions: List<Action>? = null

    @SerializedName("legendary_actions")
    @Expose
    var legendaryActions: List<LegendaryAction>? = null
}
