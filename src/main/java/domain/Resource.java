package domain;


import jakarta.persistence.*;

@Entity
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    private String name;
    private int value;
    @Enumerated(EnumType.STRING)
    private ResourceType type;

    public Resource() {}

    public Resource(String name, int value, ResourceType type) {
        this.name = name;
        this.value = value;
        this.type = type;
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
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public ResourceType getType() {
        return type;
    }



}
