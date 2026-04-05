package com.mitgrandregency.hotel.service;

import com.mitgrandregency.hotel.model.AppState;
import com.mitgrandregency.hotel.model.HistoryRecord;
import com.mitgrandregency.hotel.model.Room;

/**
 * Reporting and dashboard metrics service.
 * Computes revenue, occupancy, and summary statistics from the AppState.
 */
public class ReportService {

    private final AppState state;

    public ReportService(AppState state) {
        this.state = state;
    }

    /**
     * Calculates total revenue from all completed checkouts.
     */
    public double calculateTotalRevenue() {
        return state.getHistoryList().stream()
                .mapToDouble(HistoryRecord::getTotalPaid)
                .sum();
    }

    /**
     * Counts rooms with the given status.
     */
    public long countByStatus(String status) {
        return state.getRoomList().stream()
                .filter(r -> status.equals(r.getStatus()))
                .count();
    }

    /**
     * Returns the total number of rooms.
     */
    public int totalRooms() {
        return state.getRoomList().size();
    }

    /**
     * Returns the total number of completed checkouts.
     */
    public int totalCheckouts() {
        return state.getHistoryList().size();
    }

    /**
     * Formats the ledger summary string.
     */
    public String getLedgerSummary() {
        double totalRev = calculateTotalRevenue();
        return String.format("Total Revenue: \u20B9%.2f  |  Total Checkouts: %d",
                totalRev, totalCheckouts());
    }
}
