package com.example.demo1.services;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.example.demo1.entity.Booking;
import com.example.demo1.entity.PlanBooking;
import com.example.demo1.controller.dao.BookingDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class BookingService {
    private static final String BOOKINGS_DIR = System.getProperty("user.home") + "/.smarttrip/";
    private static final String HOTEL_BOOKINGS_FILE = BOOKINGS_DIR + "hotel_bookings.json";
    private static final String PLAN_BOOKINGS_FILE = BOOKINGS_DIR + "plan_bookings.json";

    private static BookingService instance;
    private final ObservableList<Booking> hotelBookings = FXCollections.observableArrayList();
    private final ObservableList<PlanBooking> planBookings = FXCollections.observableArrayList();
    private final ObjectMapper mapper;
    private final BookingDAO bookingDAO = new BookingDAO();

    private BookingService() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        loadBookings();
    }

    public static synchronized BookingService getInstance() {
        if (instance == null) {
            instance = new BookingService();
        }
        return instance;
    }

    private void loadBookings() {
        try {
            File dir = new File(BOOKINGS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File hotelFile = new File(HOTEL_BOOKINGS_FILE);
            if (hotelFile.exists()) {
                Booking[] bookings = mapper.readValue(hotelFile, Booking[].class);
                hotelBookings.addAll(Arrays.asList(bookings));
            }

            File planFile = new File(PLAN_BOOKINGS_FILE);
            if (planFile.exists()) {
                PlanBooking[] bookings = mapper.readValue(planFile, PlanBooking[].class);
                planBookings.addAll(Arrays.asList(bookings));
            }
        } catch (IOException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
    }

    private void saveHotelBookings() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(HOTEL_BOOKINGS_FILE), hotelBookings.toArray(new Booking[0]));
        } catch (IOException e) {
            System.err.println("Error saving hotel bookings: " + e.getMessage());
        }
    }

    private void savePlanBookings() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(PLAN_BOOKINGS_FILE), planBookings.toArray(new PlanBooking[0]));
        } catch (IOException e) {
            System.err.println("Error saving plan bookings: " + e.getMessage());
        }
    }

    // Hotel Booking Methods
    public Booking addHotelBooking(String hotelId, String hotelName, String roomType, int roomTypeId,
                                   LocalDate checkIn, LocalDate checkOut, int guestCount,
                                   double totalPrice, String guestFirstName, String guestLastName,
                                   String guestEmail, String guestPhone, String specialRequests) {
        Booking booking = new Booking();
        booking.setHotelId(hotelId);
        booking.setHotelName(hotelName);
        booking.setRoomType(roomType);
        booking.setRoomTypeId(roomTypeId);
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);
        booking.setGuestCount(guestCount);
        booking.setTotalPrice(totalPrice);
        booking.setGuestFirstName(guestFirstName);
        booking.setGuestLastName(guestLastName);
        booking.setGuestEmail(guestEmail);
        booking.setGuestPhone(guestPhone);
        booking.setSpecialRequests(specialRequests);
        booking.setStatus(Booking.Status.CONFIRMED);
        booking.setCreatedAt(LocalDate.now());

        // If user is logged in, persist to database
        if (AuthService.getInstance().isLoggedIn()) {
            String dbId = bookingDAO.create(booking, Integer.parseInt(AuthService.getInstance().getCurrentUser().getId()));
            if (dbId != null) {
                booking.setId(dbId);
            } else {
                booking.setId(generateBookingId("HB"));
            }
        } else {
            booking.setId(generateBookingId("HB"));
        }

        hotelBookings.add(0, booking);
        saveHotelBookings();
        return booking;
    }

    public void cancelHotelBooking(String bookingId) {
        for (Booking booking : hotelBookings) {
            if (booking.getId().equals(bookingId)) {
                booking.setStatus(Booking.Status.CANCELLED);
                try {
                    bookingDAO.cancel(Integer.parseInt(bookingId));
                } catch (NumberFormatException e) {
                    // id was from JSON (e.g. HB123)
                }
                saveHotelBookings();
                break;
            }
        }
    }

    /** Load hotel bookings from DB for the current user (call after login). */
    public void loadHotelBookingsFromDb() {
        if (!AuthService.getInstance().isLoggedIn()) return;
        int userId = Integer.parseInt(AuthService.getInstance().getCurrentUser().getId());
        List<Booking> fromDb = bookingDAO.getByUserId(userId);
        hotelBookings.clear();
        hotelBookings.addAll(fromDb);
    }

    public ObservableList<Booking> getHotelBookings() {
        return hotelBookings;
    }

    public ObservableList<Booking> getActiveHotelBookings() {
        return hotelBookings.filtered(b -> b.getStatus() == Booking.Status.CONFIRMED);
    }

    // Plan Booking Methods
    public PlanBooking addPlanBooking(String planId, String planName, String userEmail,
                                      double price, String duration) {
        PlanBooking booking = new PlanBooking();
        booking.setId(generateBookingId("PB"));
        booking.setPlanId(planId);
        booking.setPlanName(planName);
        booking.setUserEmail(userEmail);
        booking.setPrice(price);
        booking.setDuration(duration);
        booking.setStartDate(LocalDate.now());
        booking.setStatus(PlanBooking.Status.ACTIVE);
        booking.setCreatedAt(LocalDate.now());

        planBookings.add(0, booking);
        savePlanBookings();
        return booking;
    }

    public void cancelPlanBooking(String bookingId) {
        for (PlanBooking booking : planBookings) {
            if (booking.getId().equals(bookingId)) {
                booking.setStatus(PlanBooking.Status.CANCELLED);
                savePlanBookings();
                break;
            }
        }
    }

    public ObservableList<PlanBooking> getPlanBookings() {
        return planBookings;
    }

    public ObservableList<PlanBooking> getActivePlanBookings() {
        return planBookings.filtered(b -> b.getStatus() == PlanBooking.Status.ACTIVE);
    }

    // Statistics
    public int getTotalActiveBookings() {
        return (int) hotelBookings.stream().filter(b -> b.getStatus() == Booking.Status.CONFIRMED).count() +
                (int) planBookings.stream().filter(b -> b.getStatus() == PlanBooking.Status.ACTIVE).count();
    }

    public double getTotalSpent() {
        return hotelBookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.CONFIRMED)
                .mapToDouble(Booking::getTotalPrice)
                .sum() +
                planBookings.stream()
                        .filter(b -> b.getStatus() == PlanBooking.Status.ACTIVE)
                        .mapToDouble(PlanBooking::getPrice)
                        .sum();
    }

    private String generateBookingId(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}

