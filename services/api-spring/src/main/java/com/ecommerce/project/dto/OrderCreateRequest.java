package com.ecommerce.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull
    private Long addressId;

    @NotBlank
    @Size(min = 4)
    private String paymentMethod;

    @NotBlank
    private String pgName;

    private String pgPaymentId;

    private String pgStatus;

    private String pgResponse;
}
