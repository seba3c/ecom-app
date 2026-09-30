package com.ecommerce.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetail {

    private Long id;
    private String method;
    private String pgName;
    private String pgPaymentId;
    private String pgStatus;
    private String pgResponse;
}
