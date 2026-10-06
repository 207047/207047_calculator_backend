package com.calc.dto;

import jakarta.validation.constraints.NotBlank;

public class CalculateRequest {

    @NotBlank(message = "Expression cannot be empty")
    private String expression;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
