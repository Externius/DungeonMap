package externius.rdmg.models;



public class TrapDescription {
    private String name;
    private String description;
    public TrapDescription(String name, String description){
        setName(name);
        setDescription(description);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }
}
