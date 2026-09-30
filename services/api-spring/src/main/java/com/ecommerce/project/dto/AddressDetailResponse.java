package com.ecommerce.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDetailResponse {
    private Long id;
    private String streetLine1;
    private String streetLine2;
    private String city;
    private String state;
    private String country;
    private String zipCode;
}
