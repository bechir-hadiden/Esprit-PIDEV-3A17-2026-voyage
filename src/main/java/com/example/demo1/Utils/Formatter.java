package com.example.demo1.Utils;


import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Formatter {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter SHORT_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd");

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatCurrencyNoCents(double amount) {
        return String.format("$%.0f", amount);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMAT);
    }

    public static String formatShortDate(LocalDate date) {
        if (date == null) return "";
        return date.format(SHORT_DATE_FORMAT);
    }

    public static String formatRating(double rating) {
        return String.format("%.1f", rating);
    }

    public static long getDaysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    public static String formatNights(long nights) {
        return nights == 1 ? "1 night" : nights + " nights";
    }

    public static String formatGuests(int guests) {
        return guests == 1 ? "1 Guest" : guests + " Guests";
    }

    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
