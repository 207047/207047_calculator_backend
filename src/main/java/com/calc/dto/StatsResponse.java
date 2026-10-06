package com.calc.dto;

public class StatsResponse {

    private long totalCount;
    private Double lastResult;
    private String lastExpression;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public Double getLastResult() {
        return lastResult;
    }

    public void setLastResult(Double lastResult) {
        this.lastResult = lastResult;
    }

    public String getLastExpression() {
        return lastExpression;
    }

    public void setLastExpression(String lastExpression) {
        this.lastExpression = lastExpression;
    }
}
