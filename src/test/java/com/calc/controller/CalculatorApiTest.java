package com.calc.controller;

import com.calc.repository.CalculationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CalculatorApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CalculationHistoryRepository historyRepository;

    @BeforeEach
    void clean() {
        historyRepository.deleteAll();
    }

    @Test
    void calculateSavesHistory() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"(1+2)*3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.expression", is("(1+2)*3")))
                .andExpect(jsonPath("$.result", is(9.0)));

        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.total", is(1)))
                .andExpect(jsonPath("$.data.items[0].expression", is("(1+2)*3")));
    }

    @Test
    void extraFieldsInBodyAreIgnored() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"1+2*3\",\"result\":999}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is(7.0)));
    }

    @Test
    void invalidAndDivideZero() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"1++2\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Invalid expression")));

        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"8/0\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Division by zero")));
    }

    @Test
    void deleteOneAndMissing() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"5*8\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/history"))
                .andExpect(jsonPath("$.data.total", is(1)));

        long id = historyRepository.findAll().get(0).getId();
        mockMvc.perform(delete("/api/history/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/history"))
                .andExpect(jsonPath("$.data.total", is(0)));

        mockMvc.perform(delete("/api/history/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void emptyExpressionRejected() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }
}
