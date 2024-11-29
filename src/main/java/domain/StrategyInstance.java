package domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class StrategyInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private DefaultStrategy strategy;
    private Integer tot;
    private Integer maxConsecSkipped;

    public StrategyInstance(){}

    public StrategyInstance(DefaultStrategy strategy, Integer tot, Integer maxConsecSkipped) {
        if (strategy.requiresTot() && tot == null) {
            throw new IllegalArgumentException("This strategy requires a TOT value.");
        }
        if (strategy.requiresMaxConsecSkipped() && maxConsecSkipped == null) {
            throw new IllegalArgumentException("This strategy requires a max consecutive skipped sessions value.");
        }
        this.strategy = strategy;
        this.tot = tot;
        this.maxConsecSkipped = maxConsecSkipped;
    }

    public DefaultStrategy getStrategy() {
        return strategy;
    }

    public Integer getTot() {
        return tot;
    }

    public Integer getMaxConsecSkipped() {
        return maxConsecSkipped;
    }

    @Override
    public String toString() {
        return "StrategyInstance{" +
                "strategy=" + strategy +
                ", tot=" + tot +
                ", maxConsecSkipped=" + maxConsecSkipped +
                '}';
    }
}
