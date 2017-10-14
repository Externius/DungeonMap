package externius.rdmg.models;


public class RoomDescription {

    private final String name;
    private final String treasure;
    private final String monster;
    private final String doors;

    public RoomDescription(String name, String treasure, String monster, String doors) {
        this.name = name;
        this.treasure = treasure;
        this.monster = monster;
        this.doors = doors;
    }


    public String getName() {
        return name;
    }

    public String getTreasure() {
        return treasure;
    }

    public String getMonster() {
        return monster;
    }

    public String getDoors() {
        return doors;
    }
}
