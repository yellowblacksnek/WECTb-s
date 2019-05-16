package ru.snek;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Malefactor implements Comparable, Serializable {

    public static final String JULIO_NAME = "Жулио";
    public static final String SPROOTS_NAME = "Спрутс";

    public enum Condition implements Serializable{
        AWAKEN,
        ASLEEP,
        AWAKEN_AND_GROANING,
        AWAKEN_AND_FALLEN,
        AWAKEN_AND_TRYING_TO_OPEN_HANGAR_DOOR
    }

    private String name;
    private int age;
    private int x;
    private int y;
    private Date birthDate;
    private Condition condition;
    private Box.Weight abilityToLift;
    private boolean canSleep;
    private ArrayList<Thingable> pocketContent = new ArrayList<>();

    public class Knife implements Thingable, Serializable {
        public String toString() {
            return "Knife";
        }
    }

    public static class Box implements Thingable, Serializable {
        public enum Weight implements Serializable {VERY_LIGHT, LIGHT, MEDIUM, HEAVY, VERY_HEAVY, SPOSITE }
        Weight weight;
        public Box(Weight w) { weight = w; }
        public Weight getWeight() { return weight; }
        public String toString() {
            return "Box{" + "weight=" + weight + '}';
        }
    }

    public class Lamp implements Thingable, Serializable {

        private boolean hidden= false;
        private boolean isOn = false;

        public void setHidden(boolean on) { hidden = on; }
        public void setCond(boolean on) {
            isOn = on;
        }
        public boolean isHidden() { return hidden; }
        public boolean isOn() { return isOn; }
        public String toString() { return "Lamp{" + "hidden=" + hidden + ", isOn=" + isOn + '}'; }
    }

    public Malefactor() { this("Злоумышленнник"); }
    public Malefactor(String name) {
        this.name = name;
        birthDate = new Date();
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Date getBirthDate() { return birthDate; }
    public Condition getCondition() { return condition; }
    public Box.Weight getAbilityToLift() { return abilityToLift; }
    public boolean isCanSleep() { return canSleep; }
    public ArrayList<Thingable> getPocketContent() { return (ArrayList<Thingable>)pocketContent.clone(); }

    public void setName(String name) {this.name = name;}
    public void setAge(int age) {this.age = age;}
    public void setX(int x) {this.x = x;}
    public void setY(int y) {this.y = y;}
    public void setBirthDate(Date birthDate) {this.birthDate = birthDate;}
    public void setCondition(Condition condition) {this.condition = condition;}
    public void setAbilityToLift(Box.Weight abilityToLift) {this.abilityToLift = abilityToLift;}
    public void setCanSleep(boolean canSleep) {this.canSleep = canSleep;}
    public void setPocketContent(ArrayList<Thingable> pocketContent) {this.pocketContent = pocketContent;}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        Malefactor that = (Malefactor) obj;
        return Objects.equals(pocketContent, that.pocketContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pocketContent);
    }

    @Override
    public String toString() {
        return "Malefactor{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", x=" + x +
                ", y=" + y +
                ", birthDate=" + birthDate +
                ", condition=" + condition +
                ", abilityToLift=" + abilityToLift +
                ", canSleep=" + canSleep +
                ", pocketContent=" + pocketContent +
                '}';
    }

    @Override
    public int compareTo(Object obj) {
        Malefactor o = (Malefactor) obj;
        int result = 0;
        result += name.length() - o.name.length();
        if(result == 0) result += abilityToLift.compareTo(o.abilityToLift);
        if(result == 0) result += pocketContent.size() - o.pocketContent.size();
        return result;
    }
}
