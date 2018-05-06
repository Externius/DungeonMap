package externius.rdmg.models;

public class RoamingMonsterDescription {
    private final String name;
    private final String description;
    public RoamingMonsterDescription(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
