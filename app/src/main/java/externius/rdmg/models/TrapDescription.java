package externius.rdmg.models;



public class TrapDescription {
    private final String name;
    private final String description;
    public TrapDescription(String name, String description){
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
