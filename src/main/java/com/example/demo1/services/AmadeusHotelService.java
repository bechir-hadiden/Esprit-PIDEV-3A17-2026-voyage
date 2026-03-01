package com.example.demo1.services;
import com.example.demo1.entity.AmadeusHotelInfo;
import com.example.demo1.entity.RoomType;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Amadeus hotel APIs: list by city, hotel ratings.
 */
public class AmadeusHotelService {
    private final AmadeusClient client;

    public AmadeusHotelService() {
        this(new AmadeusClient());
    }

    public AmadeusHotelService(AmadeusClient client) {
        this.client = client;
    }

    public boolean isConfigured() {
        return client.isConfigured();
    }

    /** IATA city codes are 3 letters (e.g. PAR, LON, NYC). Amadeus test env often supports only a limited set. */
    private static final java.util.regex.Pattern IATA_CITY = java.util.regex.Pattern.compile("^[A-Za-z]{3}$");

    /**
     * List hotels by IATA city code (e.g. PAR, NYC, LON).
     * Optional radius in km.
     * @throws IllegalArgumentException if cityCode is not a 3-letter IATA code
     */
    public List<AmadeusHotelInfo> listHotelsByCity(String cityCode, Integer radiusKm) throws Exception {
        if (cityCode == null || cityCode.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String code = cityCode.trim().toUpperCase();
        if (!IATA_CITY.matcher(code).matches()) {
            throw new IllegalArgumentException("Use a 3-letter IATA city code (e.g. PAR for Paris, LON for London, NYC for New York). You entered: \"" + cityCode + "\".");
        }
        String query = "cityCode=" + java.net.URLEncoder.encode(code, java.nio.charset.StandardCharsets.UTF_8);
        if (radiusKm != null && radiusKm > 0) {
            query += "&radius=5"; // Amadeus uses 1-5 for radius index in some versions
            query += "&radiusUnit=KM";
        }
        JsonNode root = client.get("/v1/reference-data/locations/hotels/by-city", query);
        return parseHotelListResponse(root);
    }

    /**
     * Get sentiment-based ratings for an Amadeus hotel ID.
     * Returns overall rating and category ratings if available.
     */
    public AmadeusHotelInfo getHotelRatings(String amadeusHotelId) throws Exception {
        if (amadeusHotelId == null || amadeusHotelId.isEmpty()) {
            return null;
        }
        String path = "/v1/e-reputation/hotel-sentiments/" + amadeusHotelId;
        JsonNode root = client.get(path);
        AmadeusHotelInfo info = new AmadeusHotelInfo();
        info.setHotelId(amadeusHotelId);
        JsonNode data = root.path("data");
        if (data.isObject()) {
            if (data.has("overallRating")) {
                info.setRating(data.get("overallRating").asDouble(0));
            }
            if (data.has("ratingType")) {
                info.setRatingType(data.get("ratingType").asText(null));
            }
        }
        return info;
    }

    /**
     * Get hotel offers (rooms with prices) for an Amadeus hotel ID.
     * Uses GET /v1/shopping/hotel-offers. Returns list of RoomType for display.
     */
    public List<RoomType> getHotelOffers(String amadeusHotelId, LocalDate checkIn, LocalDate checkOut, int adults) throws Exception {
        if (amadeusHotelId == null || amadeusHotelId.isEmpty()) {
            return new ArrayList<>();
        }
        String checkInStr = checkIn != null ? checkIn.format(DateTimeFormatter.ISO_LOCAL_DATE) : LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String checkOutStr = checkOut != null ? checkOut.format(DateTimeFormatter.ISO_LOCAL_DATE) : LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE);
        int a = Math.max(1, Math.min(9, adults));
        String query = "hotelIds=" + java.net.URLEncoder.encode(amadeusHotelId, java.nio.charset.StandardCharsets.UTF_8)
                + "&adults=" + a
                + "&checkInDate=" + checkInStr
                + "&checkOutDate=" + checkOutStr;
        try {
            JsonNode root = client.get("/v3/shopping/hotel-offers", query);
            return parseHotelOffersResponse(root, checkInStr, checkOutStr);
        } catch (AmadeusApiException e) {
            // Handle specific error codes
            if (e.getStatusCode() == 400) {
                if (e.getMessage().contains("3843")) {
                    System.out.println("Hotel " + amadeusHotelId + " cannot accommodate " + a + " guests. Skipping offers.");
                    return new ArrayList<>(); // Return empty list, hotel can't accommodate this many guests
                }
                if (e.getMessage().contains("3664")) {
                    System.out.println("No rooms available at hotel " + amadeusHotelId + " for the requested dates. Skipping offers.");
                    return new ArrayList<>(); // Return empty list, no rooms available
                }
            }
            // Try v1 as fallback for 301/404
            if (e.getStatusCode() == 301 || e.getStatusCode() == 404) {
                JsonNode root = client.get("/v1/shopping/hotel-offers", query);
                return parseHotelOffersResponse(root, checkInStr, checkOutStr);
            }
            throw e;
        } catch (Exception e) {
            // Try v1 as fallback for wrapped exceptions
            if (e.getCause() instanceof AmadeusApiException cause) {
                if (cause.getStatusCode() == 301 || cause.getStatusCode() == 404) {
                    JsonNode root = client.get("/v1/shopping/hotel-offers", query);
                    return parseHotelOffersResponse(root, checkInStr, checkOutStr);
                }
            }
            throw e;
        }
    }

    private List<RoomType> parseHotelOffersResponse(JsonNode root, String checkInStr, String checkOutStr) {
        System.out.println("Parsing hotel offers response: " + root.toString());
        List<RoomType> rooms = new ArrayList<>();
        JsonNode data = root.path("data");
        if (!data.isArray()) {
            System.out.println("No array data found in response");
            return rooms;
        }
        int offerIndex = 0;
        for (JsonNode hotelNode : data) {
            System.out.println("Processing hotel node: " + hotelNode.toString());
            JsonNode offers = hotelNode.path("offers");
            if (!offers.isArray()) {
                System.out.println("No offers array found in hotel node");
                continue;
            }
            for (JsonNode offer : offers) {
                System.out.println("Processing offer: " + offer.toString());
                RoomType rt = new RoomType();
                rt.setId("am-" + offerIndex++);
                JsonNode room = offer.path("room");
                if (!room.isObject()) room = offer;
                String name = room.path("type").asText(null);
                if (name == null || name.isEmpty()) name = room.path("description").asText(null);
                if (name == null || name.isEmpty()) name = offer.path("description").asText(null);
                if (name == null || name.isEmpty()) name = offer.path("name").asText(null);
                rt.setName(name != null && !name.isEmpty() ? name : "Room " + offerIndex);
                rt.setDescription(room.path("description").asText(null));
                if (rt.getDescription() == null) rt.setDescription(offer.path("description").asText(""));
                rt.setMaxGuests(2);
                if (offer.has("guests")) rt.setMaxGuests(offer.get("guests").asInt(2));
                JsonNode price = offer.path("price");
                if (!price.isObject()) price = offer.path("total");
                if (!price.isObject()) price = offer.path("base");
                if (price.isObject()) {
                    double totalPrice = 0;
                    if (price.has("total")) {
                        if (price.get("total").isNumber()) totalPrice = price.get("total").asDouble();
                        else totalPrice = Double.parseDouble(price.get("total").asText("0").replaceAll("[^0-9.]", ""));
                    } else if (price.has("amount")) {
                        if (price.get("amount").isNumber()) totalPrice = price.get("amount").asDouble();
                        else totalPrice = Double.parseDouble(price.get("amount").asText("0").replaceAll("[^0-9.]", ""));
                    } else if (price.has("base")) {
                        if (price.get("base").isNumber()) totalPrice = price.get("base").asDouble();
                        else totalPrice = Double.parseDouble(price.get("base").asText("0").replaceAll("[^0-9.]", ""));
                    }
                    rt.setPricePerNight(totalPrice);
                }
                rt.setAvailable(true);
                rooms.add(rt);
                System.out.println("Added room: " + rt.getName() + " - Price: " + rt.getPricePerNight());
            }
        }
        System.out.println("Total rooms parsed: " + rooms.size());
        return rooms;
    }

    /**
     * Enrich a list of Amadeus hotels with ratings (best-effort; rate limits may apply).
     */
    public void fillRatings(List<AmadeusHotelInfo> hotels) {
        for (AmadeusHotelInfo h : hotels) {
            if (h.getHotelId() == null || h.getRating() != null) continue;
            try {
                AmadeusHotelInfo ratings = getHotelRatings(h.getHotelId());
                if (ratings != null && ratings.getRating() != null) {
                    h.setRating(ratings.getRating());
                }
            } catch (Exception e) {
                // skip on error to avoid breaking the list
            }
        }
    }

    private List<AmadeusHotelInfo> parseHotelListResponse(JsonNode root) {
        List<AmadeusHotelInfo> list = new ArrayList<>();
        // Amadeus can return { "data": [ ... ] }, or array at root, or other keys
        JsonNode data = root.isArray() ? root : root.path("data");
        if (!data.isArray()) data = root.path("response");
        if (!data.isArray()) data = root.path("results");
        if (!data.isArray()) return list;
        for (JsonNode item : data) {
            if (!item.isObject()) continue;
            AmadeusHotelInfo info = new AmadeusHotelInfo();
            String hotelId = item.has("hotelId") ? item.get("hotelId").asText(null) : item.path("id").asText(null);
            info.setHotelId(hotelId);
            info.setName(item.path("name").asText(null));
            info.setChainCode(item.path("chainCode").asText(null));
            info.setIataCode(item.path("iataCode").asText(null));
            if (info.getIataCode() == null || info.getIataCode().isEmpty()) {
                info.setIataCode(item.path("cityCode").asText(null));
            }
            JsonNode address = item.path("address");
            if (address.isObject()) {
                if (address.has("lines") && address.get("lines").isArray() && address.get("lines").size() > 0) {
                    info.setAddress(address.get("lines").get(0).asText(""));
                }
                info.setCityName(address.path("cityName").asText(null));
                info.setCountryCode(address.path("countryCode").asText(null));
            }
            if (info.getCityName() == null) info.setCityName(item.path("cityCode").asText(null));
            JsonNode geo = item.path("geoCode");
            if (geo.isObject()) {
                info.setLatitude(geo.has("latitude") ? geo.get("latitude").asDouble() : null);
                info.setLongitude(geo.has("longitude") ? geo.get("longitude").asDouble() : null);
            } else {
                if (item.has("latitude")) info.setLatitude(item.get("latitude").asDouble());
                if (item.has("longitude")) info.setLongitude(item.get("longitude").asDouble());
            }
            if (info.getAddress() == null && item.has("address")) {
                JsonNode addr = item.get("address");
                info.setAddress(addr.isTextual() ? addr.asText() : addr.toString());
            }
            list.add(info);
        }
        return list;
    }
}
