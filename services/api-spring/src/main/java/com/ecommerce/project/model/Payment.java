package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "payments")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Payment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 4)
    private String method;

    private String pgPaymentId;

    private String pgStatus;

    private String pgResponse;

    private String pgName;

    @OneToOne(mappedBy = "payment")
    @ToString.Exclude
    private Order order;

    public Payment(String method, String pgName, String pgPaymentId, String pgStatus, String pgResponse) {
        this.method = method;
        this.pgName = pgName;
        this.pgPaymentId = pgPaymentId;
        this.pgStatus = pgStatus;
        this.pgResponse = pgResponse;
    }
}
