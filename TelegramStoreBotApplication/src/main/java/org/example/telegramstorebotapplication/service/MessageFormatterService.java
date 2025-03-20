package org.example.telegramstorebotapplication.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MessageFormatterService {

    public String formatStatistics(Map statistics, String period) {
        if (statistics == null || statistics.isEmpty()) {
            return "No statistics available for the " + period + " period.";
        }

        StringBuilder message = new StringBuilder();
        message.append("*").append(period).append(" Statistics*\n\n");

        try {
            // Format sold/unsold products
            if (statistics.containsKey("soldProducts")) {
                message.append("*Sold Products:* ").append(statistics.get("soldProducts")).append("\n");
            }

            if (statistics.containsKey("unsoldProducts")) {
                message.append("*Unsold Products:* ").append(statistics.get("unsoldProducts")).append("\n");
            }

            // Format most expensive/cheapest items
            if (statistics.containsKey("mostExpensiveItem")) {
                Map<String, Object> mostExpensive = (Map<String, Object>) statistics.get("mostExpensiveItem");
                message.append("\n*Most Expensive Item:*\n");
                message.append("- Name: ").append(mostExpensive.get("name")).append("\n");
                message.append("- Price: $").append(mostExpensive.get("price")).append("\n");
            }

            if (statistics.containsKey("cheapestItem")) {
                Map<String, Object> cheapest = (Map<String, Object>) statistics.get("cheapestItem");
                message.append("\n*Cheapest Item:*\n");
                message.append("- Name: ").append(cheapest.get("name")).append("\n");
                message.append("- Price: $").append(cheapest.get("price")).append("\n");
            }

            // Format top selling products if available
            if (statistics.containsKey("topSellingProducts")) {
                List<Map<String, Object>> topSelling = (List<Map<String, Object>>) statistics.get("topSellingProducts");
                if (!topSelling.isEmpty()) {
                    message.append("\n*Top Selling Products:*\n");
                    int count = 1;
                    for (Map<String, Object> product : topSelling) {
                        message.append(count).append(". ").append(product.get("name"))
                                .append(" - $").append(product.get("price"))
                                .append(" (Sold: ").append(product.get("quantitySold")).append(")\n");
                        count++;
                    }
                }
            }

            // Format revenue information
            if (statistics.containsKey("totalRevenue")) {
                message.append("\n*Total Revenue:* $").append(statistics.get("totalRevenue")).append("\n");
            }

            if (statistics.containsKey("averageOrderValue")) {
                message.append("*Average Order Value:* $").append(statistics.get("averageOrderValue")).append("\n");
            }

        } catch (Exception e) {
            log.error("Error formatting statistics: {}", e.getMessage());
            return "Error formatting statistics. Please try again later.";
        }

        return message.toString();
    }

    public String formatProductStatistics(Map statistics, String period) {
        if (statistics == null || statistics.isEmpty()) {
            return "No product statistics available for the " + period + " period.";
        }

        StringBuilder message = new StringBuilder();
        message.append("*").append(period).append(" Product Statistics*\n\n");

        try {
            // Product-specific statistics
            if (statistics.containsKey("totalProducts")) {
                message.append("*Total Products:* ").append(statistics.get("totalProducts")).append("\n");
            }

            if (statistics.containsKey("newProducts")) {
                message.append("*New Products:* ").append(statistics.get("newProducts")).append("\n");
            }

            if (statistics.containsKey("productCategories")) {
                Map<String, Integer> categories = (Map<String, Integer>) statistics.get("productCategories");
                message.append("\n*Product Categories:*\n");
                for (Map.Entry<String, Integer> entry : categories.entrySet()) {
                    message.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }

            // Include any product-specific data from the general statistics
            if (statistics.containsKey("soldProducts")) {
                message.append("\n*Sold Products:* ").append(statistics.get("soldProducts")).append("\n");
            }

            if (statistics.containsKey("unsoldProducts")) {
                message.append("*Unsold Products:* ").append(statistics.get("unsoldProducts")).append("\n");
            }

        } catch (Exception e) {
            log.error("Error formatting product statistics: {}", e.getMessage());
            return "Error formatting product statistics. Please try again later.";
        }

        return message.toString();
    }

    public String formatOrderStatistics(Map statistics, String period) {
        if (statistics == null || statistics.isEmpty()) {
            return "No order statistics available for the " + period + " period.";
        }

        StringBuilder message = new StringBuilder();
        message.append("*").append(period).append(" Order Statistics*\n\n");

        try {
            // Order-specific statistics
            if (statistics.containsKey("totalOrders")) {
                message.append("*Total Orders:* ").append(statistics.get("totalOrders")).append("\n");
            }

            if (statistics.containsKey("averageOrderValue")) {
                message.append("*Average Order Value:* $").append(statistics.get("averageOrderValue")).append("\n");
            }

            if (statistics.containsKey("totalRevenue")) {
                message.append("*Total Revenue:* $").append(statistics.get("totalRevenue")).append("\n");
            }

            if (statistics.containsKey("ordersByStatus")) {
                Map<String, Integer> orderStatus = (Map<String, Integer>) statistics.get("ordersByStatus");
                message.append("\n*Orders by Status:*\n");
                for (Map.Entry<String, Integer> entry : orderStatus.entrySet()) {
                    message.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }

            if (statistics.containsKey("ordersByPaymentMethod")) {
                Map<String, Integer> paymentMethods = (Map<String, Integer>) statistics.get("ordersByPaymentMethod");
                message.append("\n*Orders by Payment Method:*\n");
                for (Map.Entry<String, Integer> entry : paymentMethods.entrySet()) {
                    message.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }

        } catch (Exception e) {
            log.error("Error formatting order statistics: {}", e.getMessage());
            return "Error formatting order statistics. Please try again later.";
        }

        return message.toString();
    }
}

