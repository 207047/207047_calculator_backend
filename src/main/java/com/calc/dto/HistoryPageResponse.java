package com.calc.dto;

import java.util.List;

public class HistoryPageResponse {

    private List<HistoryItemResponse> items;
    private long total;
    private int page;
    private int size;

    public List<HistoryItemResponse> getItems() {
        return items;
    }

    public void setItems(List<HistoryItemResponse> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
