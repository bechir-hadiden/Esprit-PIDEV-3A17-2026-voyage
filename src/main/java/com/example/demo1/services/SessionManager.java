package com.example.demo1.services;

import com.example.demo1.entity.Hotel;
import com.example.demo1.entity.RoomType;
import com.example.demo1.entity.SearchFilters;
import javafx.beans.property.*;

public class SessionManager {
    private static SessionManager instance;

    // Current view state
    private final ObjectProperty<View> currentView = new SimpleObjectProperty<>(View.SIGN_IN);

    // Selected hotel and room for booking flow
    private final ObjectProperty<Hotel> selectedHotel = new SimpleObjectProperty<>();
    private final ObjectProperty<RoomType> selectedRoom = new SimpleObjectProperty<>();

    // Search filters
    private final ObjectProperty<SearchFilters> searchFilters = new SimpleObjectProperty<>(new SearchFilters());

    // Sidebar collapsed state
    private final BooleanProperty sidebarCollapsed = new SimpleBooleanProperty(false);

    public enum View {
        SIGN_IN, SIGN_UP, FORGOT_PASSWORD, DASHBOARD, HOTELS, HOTEL_DETAILS, BOOKING,
        TRANSPORT, OFFERS, TICKET_PLANS, MY_BOOKINGS, SETTINGS ,RECLAMATION
        ,MY_WALLET
    }

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // View management
    public View getCurrentView() { return currentView.get(); }
    public void setCurrentView(View view) { currentView.set(view); }
    public ObjectProperty<View> currentViewProperty() { return currentView; }

    // Selected hotel
    public Hotel getSelectedHotel() { return selectedHotel.get(); }
    public void setSelectedHotel(Hotel hotel) { selectedHotel.set(hotel); }
    public ObjectProperty<Hotel> selectedHotelProperty() { return selectedHotel; }

    // Selected room
    public RoomType getSelectedRoom() { return selectedRoom.get(); }
    public void setSelectedRoom(RoomType room) { selectedRoom.set(room); }
    public ObjectProperty<RoomType> selectedRoomProperty() { return selectedRoom; }

    // Search filters
    public SearchFilters getSearchFilters() { return searchFilters.get(); }
    public void setSearchFilters(SearchFilters filters) { searchFilters.set(filters); }
    public ObjectProperty<SearchFilters> searchFiltersProperty() { return searchFilters; }

    // Sidebar collapsed
    public boolean isSidebarCollapsed() { return sidebarCollapsed.get(); }
    public void setSidebarCollapsed(boolean collapsed) { sidebarCollapsed.set(collapsed); }
    public BooleanProperty sidebarCollapsedProperty() { return sidebarCollapsed; }

    // Reset booking selection
    public void clearBookingSelection() {
        selectedHotel.set(null);
        selectedRoom.set(null);
    }
}
