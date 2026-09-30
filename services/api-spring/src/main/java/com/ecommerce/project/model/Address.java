package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "addresses")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Address extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 1, message = "Street name must have at least 1 characters")
    private String streetLine1;

    private String streetLine2;

    @NotBlank
    @Size(min = 3, message = "City name must have at least 3 characters")
    private String city;

    @NotBlank
    @Size(min = 3, message = "State name must have at least 3 characters")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country name must have at least 2 characters")
    private String country;

    @NotBlank
    private String zipCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;
}
