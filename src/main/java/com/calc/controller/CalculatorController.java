package com.calc.controller;

import com.calc.dto.ApiResponse;
import com.calc.dto.CalculateRequest;
import com.calc.dto.HistoryItemResponse;
import com.calc.dto.HistoryPageResponse;
import com.calc.dto.StatsResponse;
import com.calc.exception.CalculatorException;
import com.calc.model.CalculationHistory;
import com.calc.service.CalculatorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/calculate")
    public ApiResponse<Void> calculate(@Valid @RequestBody CalculateRequest request) {
        CalculationHistory saved = calculatorService.calculateAndSave(request.getExpression());
        return ApiResponse.ok(saved.getExpression(), saved.getResult());
    }

    @GetMapping("/history")
    public ApiResponse<HistoryPageResponse> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.okData(calculatorService.listHistory(page, size, keyword));
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteOne(@PathVariable Long id) {
        calculatorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> deleteAll() {
        calculatorService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/history/{id}/favorite")
    public ApiResponse<HistoryItemResponse> favorite(@PathVariable Long id) {
        return ApiResponse.okData(calculatorService.toggleFavorite(id));
    }

    @GetMapping("/stats")
    public ApiResponse<StatsResponse> stats() {
        return ApiResponse.okData(calculatorService.stats());
    }

    @PostMapping("/convert-base")
    public ApiResponse<Map<String, String>> convertBase(@RequestBody(required = false) Map<String, String> body) {
        try {
            if (body == null) {
                throw new CalculatorException("Invalid number for selected base");
            }
            String value = body.getOrDefault("value", "");
            int fromBase = Integer.parseInt(body.getOrDefault("fromBase", "10"));
            int toBase = Integer.parseInt(body.getOrDefault("toBase", "2"));
            String converted = calculatorService.convertBaseText(value, fromBase, toBase);
            return ApiResponse.okData(Map.of(
                    "value", value,
                    "fromBase", String.valueOf(fromBase),
                    "toBase", String.valueOf(toBase),
                    "result", converted
            ));
        } catch (NumberFormatException ex) {
            throw new CalculatorException("Invalid number for selected base");
        }
    }
}
