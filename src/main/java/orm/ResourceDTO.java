package orm;

import domain.Resource;
import domain.ResourceType;

public class ResourceDTO {
    private final long id;
    private String name;
    private int value;
    private Integer money;
    private ResourceType type;


    public ResourceDTO() {
        this.id = -1; // Valore di default
    }

    public ResourceDTO(Resource resource) {
        this.id = resource.getId();
        this.name = resource.getName();
        this.type = resource.getType();

        if (resource.getType() == ResourceType.MONEY) {
            this.money = resource.getMoney();
            this.value = resource.calculateValueFromMoney();
        } else {
            this.value = resource.getValue();
        }
    }
    // Getter
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
    public void setValue(int value){
        this.value = value;
    }

    public Integer getMoney() {
        return money;
    }

    public ResourceType getType() {
        return type;
    }

    // Setter (solo per i campi modificabili)
    public void setName(String name) {
        this.name = name;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

}

