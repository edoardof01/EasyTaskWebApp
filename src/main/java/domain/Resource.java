package domain;

import java.util.Objects;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

@Entity
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String name;

    // Per COMPETENCE e EQUIPMENT
    private Integer value;

    // Per MONEY
    private Integer money;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    public Resource() {}

    public Resource(String name,ResourceType type, @Nullable Integer value,  @Nullable Integer money) {
        if (type == ResourceType.MONEY) {
            if (money == null) {
                throw new IllegalArgumentException("Money cannot be null for MONEY resources");
            }
            /*if (value != null ) {
                throw new IllegalArgumentException("Value should not be set for MONEY resources");
            }*/
        } else {
            if (money != null) {
                throw new IllegalArgumentException("Money should not be set for non-MONEY resources");
            }
            if (value == null || value <= 0 || value > 5) {
                throw new IllegalArgumentException("Value for competences and equipment must be between 1 and 5");
            }
        }

        this.name = name;
        this.value = value;
        this.type = type;
        this.money = money;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getValue() {
        if (type == ResourceType.MONEY) {
            return calculateValueFromMoney();
        }
        return value;
    }
    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer calculateValueFromMoney() {
        if (money <= 100) {
            return 1;
        } else if (money <= 500) {
            return 2;
        } else if (money <= 1000) {
            return 3;
        } else if (money <= 3000) {
            return 4;
        } else {
            return 5;
        }
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
    public void setType(ResourceType type) {
        this.type = type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(name, resource.name) &&
                type == resource.type &&
                Objects.equals(value, resource.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, value);
    }

}











