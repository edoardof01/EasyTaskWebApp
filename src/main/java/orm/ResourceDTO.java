package orm;

import domain.Resource;
import domain.ResourceType;
import jakarta.persistence.Enumerated;

public class ResourceDTO {
    private final long id;
    private String name;
    private int value;
    private Integer money;
    @Enumerated
    private ResourceType type;

    public ResourceDTO(Resource resource) {
        this.id = resource.getId();
        this.name = resource.getName();
        this.value = resource.getValue();
        this.money = resource.getMoney();
        this.type = resource.getType();
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {}
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public Integer getMoney() {
        return money;
    }
    public void setMoney(Integer money) {
        this.money = money;
    }
    public ResourceType getType() {
        return type;
    }

}
