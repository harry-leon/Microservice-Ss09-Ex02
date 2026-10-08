package com.rikkei.microservice.pharmacyservice.controller;

import com.rikkei.microservice.pharmacyservice.dto.BillRequest;
import com.rikkei.microservice.pharmacyservice.dto.BillResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RefreshScope
public class BillController {

    @Value("${pharmacy.vat-rate:0.08}")
    private double vatRate;

    /**
     * POST /api/v1/bill
     * Tinh tien hoa don thuoc theo cong thuc:
     * tong tien thuoc + % thue VAT cua tong tien thuoc
     */
    @PostMapping("/bill")
    public ResponseEntity<BillResponse> calculateBill(@Valid @RequestBody BillRequest request) {
        BigDecimal totalMedicineAmount = request.getTotalMedicineAmount();
        BigDecimal vatRateMultiplier = BigDecimal.valueOf(vatRate);

        // VAT amount = totalMedicineAmount * vatRate
        BigDecimal vatAmount = totalMedicineAmount.multiply(vatRateMultiplier).setScale(2, RoundingMode.HALF_UP);

        // Final amount = totalMedicineAmount + vatAmount
        BigDecimal finalAmount = totalMedicineAmount.add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        BillResponse response = BillResponse.builder()
                .totalMedicineAmount(totalMedicineAmount)
                .vatRate(vatRate)
                .vatAmount(vatAmount)
                .finalAmount(finalAmount)
                .message("Bill calculated successfully with current VAT rate: " + (vatRate * 100) + "%")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/bill/vat-rate
     * Tien ich giup kiem tra ty le VAT hien tai dang duoc ap dung.
     */
    @GetMapping("/bill/vat-rate")
    public ResponseEntity<Map<String, Object>> getCurrentVatRate() {
        return ResponseEntity.ok(Map.of(
                "vatRate", vatRate,
                "vatRatePercent", (vatRate * 100) + "%"
        ));
    }
}
