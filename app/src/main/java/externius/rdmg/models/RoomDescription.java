package externius.rdmg.models;


public class RoomDescription {

    private String name;
    private String treasure;
    private String monster;


    public RoomDescription(String name, String treasure, String monster) {
        setName(name);
        setTreasure(treasure);
        setMonster(monster);
    }


    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getTreasure() {
        return treasure;
    }

    private void setTreasure(String treasure) {
        this.treasure = treasure;
    }

    public String getMonster() {
        return monster;
    }

    private void setMonster(String monster) {
        this.monster = monster;
    }
}
