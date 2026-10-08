package com.rikkei.microservice.pharmacyservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillRequest {

    /**
     * Total medicine amount before VAT.
     */
    @NotNull(message = "Total medicine amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total medicine amount must be greater than 0")
    private BigDecimal totalMedicineAmount;
}
