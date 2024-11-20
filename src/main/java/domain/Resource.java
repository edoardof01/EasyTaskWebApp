package domain;


import jakarta.annotation.Nullable;
import jakarta.persistence.*;

@Entity
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String name;
    private int value;
    private Integer money;

    @Enumerated(EnumType.STRING)
    private ResourceType type;



    public Resource() {}

    public Resource(String name, int value, ResourceType type, @Nullable Integer money) {
        if(type!=ResourceType.MONEY && !(value>0 && value<=5)){
            throw new IllegalArgumentException("competences and equipment must be between 1 and 5");
        }
        if(type!=ResourceType.MONEY && money!=null){
            throw new IllegalArgumentException("an equipment or a competence can't have a price");
        }
        this.name = name;
        this.value = value;
        this.type = type;
        this.money = money;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getValue() {
        if (this.type == ResourceType.MONEY) {
            if(value>0 && value<100){
                value=1;
                return value;
            }
            if (value>100 && value<500){
                value=2;
                return value;
            }
            if(value>500 && value<1000){
                value=3;
                return value;
            }
            if(value>1000 && value<3000){
                value=4;
            }
            if(value>3000){
                value=5;
                return value;
            }
        }
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
    public ResourceType getType() {
        return type;
    }
    public Integer getMoney() {
        return money;
    }
    public void setMoney(int money) {
        this.money = money;
    }



}
