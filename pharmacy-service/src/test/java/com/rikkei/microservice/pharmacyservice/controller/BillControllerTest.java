package com.rikkei.microservice.pharmacyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikkei.microservice.pharmacyservice.dto.BillRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "pharmacy.vat-rate=0.10"
})
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCalculateBillWithVat() throws Exception {
        BillRequest request = BillRequest.builder()
                .totalMedicineAmount(BigDecimal.valueOf(100.00))
                .build();

        // 100 + (100 * 0.10) = 110.00
        mockMvc.perform(post("/api/v1/bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMedicineAmount").value(100.0))
                .andExpect(jsonPath("$.vatRate").value(0.10))
                .andExpect(jsonPath("$.vatAmount").value(10.0))
                .andExpect(jsonPath("$.finalAmount").value(110.0));
    }

    @Test
    void testGetCurrentVatRate() throws Exception {
        mockMvc.perform(get("/api/v1/bill/vat-rate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vatRate").value(0.10));
    }
}
