package com.rikkei.microservice.pharmacyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponse {
    private BigDecimal totalMedicineAmount;
    private double vatRate;
    private BigDecimal vatAmount;
    private BigDecimal finalAmount;
    private String message;
}
