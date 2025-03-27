package entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "value")
    private double value;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

}
