package com.ecommerce.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateRequest {

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
}
