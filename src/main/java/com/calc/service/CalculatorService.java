package com.calc.service;

import com.calc.dto.HistoryItemResponse;
import com.calc.dto.HistoryPageResponse;
import com.calc.dto.StatsResponse;
import com.calc.exception.CalculatorException;
import com.calc.model.CalculationHistory;
import com.calc.repository.CalculationHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalculatorService {

    private final ExpressionEngine engine = new ExpressionEngine();
    private final CalculationHistoryRepository historyRepository;

    public CalculatorService(CalculationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Transactional
    public CalculationHistory calculateAndSave(String expression) {
        double result = engine.evaluate(expression);
        CalculationHistory history = new CalculationHistory();
        history.setExpression(expression.trim());
        history.setResult(round(result));
        return historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public HistoryPageResponse listHistory(int page, int size, String keyword) {
        int safePage = Math.max(page, 1) - 1;
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CalculationHistory> result;
        if (keyword == null || keyword.isBlank()) {
            result = historyRepository.findAll(pageable);
        } else {
            result = historyRepository.findByExpressionContainingIgnoreCase(keyword.trim(), pageable);
        }
        HistoryPageResponse response = new HistoryPageResponse();
        response.setItems(result.getContent().stream().map(this::toItem).toList());
        response.setTotal(result.getTotalElements());
        response.setPage(page <= 0 ? 1 : page);
        response.setSize(safeSize);
        return response;
    }

    @Transactional
    public void deleteById(Long id) {
        if (!historyRepository.existsById(id)) {
            throw new CalculatorException("History record not found");
        }
        historyRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        historyRepository.deleteAll();
    }

    @Transactional
    public HistoryItemResponse toggleFavorite(Long id) {
        CalculationHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new CalculatorException("History record not found"));
        history.setFavorite(!history.isFavorite());
        return toItem(historyRepository.save(history));
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        StatsResponse stats = new StatsResponse();
        long count = historyRepository.count();
        stats.setTotalCount(count);
        if (count > 0) {
            CalculationHistory latest = historyRepository
                    .findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")))
                    .getContent()
                    .get(0);
            stats.setLastExpression(latest.getExpression());
            stats.setLastResult(latest.getResult());
        }
        return stats;
    }

    public String convertBaseText(String value, int fromBase, int toBase) {
        if (fromBase < 2 || fromBase > 36 || toBase < 2 || toBase > 36) {
            throw new CalculatorException("Base must be between 2 and 36");
        }
        try {
            long number = Long.parseLong(value.trim(), fromBase);
            return Long.toString(number, toBase).toUpperCase();
        } catch (NumberFormatException ex) {
            throw new CalculatorException("Invalid number for selected base");
        }
    }

    private HistoryItemResponse toItem(CalculationHistory entity) {
        HistoryItemResponse item = new HistoryItemResponse();
        item.setId(entity.getId());
        item.setExpression(entity.getExpression());
        item.setResult(entity.getResult());
        item.setCreatedAt(entity.getCreatedAt());
        item.setFavorite(entity.isFavorite());
        return item;
    }

    private double round(double value) {
        return Math.round(value * 1_000_000_000d) / 1_000_000_000d;
    }
}
