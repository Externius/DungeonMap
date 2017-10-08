package externius.rdmg.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Monster {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("size")
    @Expose
    private String size;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("subtype")
    @Expose
    private String subtype;
    @SerializedName("alignment")
    @Expose
    private String alignment;
    @SerializedName("armor_class")
    @Expose
    private Integer armorClass;
    @SerializedName("hit_points")
    @Expose
    private Integer hitPoints;
    @SerializedName("hit_dice")
    @Expose
    private String hitDice;
    @SerializedName("speed")
    @Expose
    private String speed;
    @SerializedName("strength")
    @Expose
    private Integer strength;
    @SerializedName("dexterity")
    @Expose
    private Integer dexterity;
    @SerializedName("constitution")
    @Expose
    private Integer constitution;
    @SerializedName("intelligence")
    @Expose
    private Integer intelligence;
    @SerializedName("wisdom")
    @Expose
    private Integer wisdom;
    @SerializedName("charisma")
    @Expose
    private Integer charisma;
    @SerializedName("constitution_save")
    @Expose
    private Integer constitutionSave;
    @SerializedName("intelligence_save")
    @Expose
    private Integer intelligenceSave;
    @SerializedName("wisdom_save")
    @Expose
    private Integer wisdomSave;
    @SerializedName("history")
    @Expose
    private Integer history;
    @SerializedName("perception")
    @Expose
    private Integer perception;
    @SerializedName("damage_vulnerabilities")
    @Expose
    private String damageVulnerabilities;
    @SerializedName("damage_resistances")
    @Expose
    private String damageResistances;
    @SerializedName("damage_immunities")
    @Expose
    private String damageImmunities;
    @SerializedName("condition_immunities")
    @Expose
    private String conditionImmunities;
    @SerializedName("senses")
    @Expose
    private String senses;
    @SerializedName("languages")
    @Expose
    private String languages;
    @SerializedName("challenge_rating")
    @Expose
    private String challengeRating;
    @SerializedName("special_abilities")
    @Expose
    private List<SpecialAbility> specialAbilities = null;
    @SerializedName("actions")
    @Expose
    private List<Action> actions = null;
    @SerializedName("legendary_actions")
    @Expose
    private List<LegendaryAction> legendaryActions = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public Integer getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(Integer armorClass) {
        this.armorClass = armorClass;
    }

    public Integer getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(Integer hitPoints) {
        this.hitPoints = hitPoints;
    }

    public String getHitDice() {
        return hitDice;
    }

    public void setHitDice(String hitDice) {
        this.hitDice = hitDice;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public Integer getStrength() {
        return strength;
    }

    public void setStrength(Integer strength) {
        this.strength = strength;
    }

    public Integer getDexterity() {
        return dexterity;
    }

    public void setDexterity(Integer dexterity) {
        this.dexterity = dexterity;
    }

    public Integer getConstitution() {
        return constitution;
    }

    public void setConstitution(Integer constitution) {
        this.constitution = constitution;
    }

    public Integer getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(Integer intelligence) {
        this.intelligence = intelligence;
    }

    public Integer getWisdom() {
        return wisdom;
    }

    public void setWisdom(Integer wisdom) {
        this.wisdom = wisdom;
    }

    public Integer getCharisma() {
        return charisma;
    }

    public void setCharisma(Integer charisma) {
        this.charisma = charisma;
    }

    public Integer getConstitutionSave() {
        return constitutionSave;
    }

    public void setConstitutionSave(Integer constitutionSave) {
        this.constitutionSave = constitutionSave;
    }

    public Integer getIntelligenceSave() {
        return intelligenceSave;
    }

    public void setIntelligenceSave(Integer intelligenceSave) {
        this.intelligenceSave = intelligenceSave;
    }

    public Integer getWisdomSave() {
        return wisdomSave;
    }

    public void setWisdomSave(Integer wisdomSave) {
        this.wisdomSave = wisdomSave;
    }

    public Integer getHistory() {
        return history;
    }

    public void setHistory(Integer history) {
        this.history = history;
    }

    public Integer getPerception() {
        return perception;
    }

    public void setPerception(Integer perception) {
        this.perception = perception;
    }

    public String getDamageVulnerabilities() {
        return damageVulnerabilities;
    }

    public void setDamageVulnerabilities(String damageVulnerabilities) {
        this.damageVulnerabilities = damageVulnerabilities;
    }

    public String getDamageResistances() {
        return damageResistances;
    }

    public void setDamageResistances(String damageResistances) {
        this.damageResistances = damageResistances;
    }

    public String getDamageImmunities() {
        return damageImmunities;
    }

    public void setDamageImmunities(String damageImmunities) {
        this.damageImmunities = damageImmunities;
    }

    public String getConditionImmunities() {
        return conditionImmunities;
    }

    public void setConditionImmunities(String conditionImmunities) {
        this.conditionImmunities = conditionImmunities;
    }

    public String getSenses() {
        return senses;
    }

    public void setSenses(String senses) {
        this.senses = senses;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getChallengeRating() {
        return challengeRating;
    }

    public void setChallengeRating(String challengeRating) {
        this.challengeRating = challengeRating;
    }

    public List<SpecialAbility> getSpecialAbilities() {
        return specialAbilities;
    }

    public void setSpecialAbilities(List<SpecialAbility> specialAbilities) {
        this.specialAbilities = specialAbilities;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<LegendaryAction> getLegendaryActions() {
        return legendaryActions;
    }

    public void setLegendaryActions(List<LegendaryAction> legendaryActions) {
        this.legendaryActions = legendaryActions;
    }

}