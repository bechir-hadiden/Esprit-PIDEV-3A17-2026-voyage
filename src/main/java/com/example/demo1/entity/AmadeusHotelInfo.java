package com.example.demo1.entity;

public class AmadeusHotelInfo {
    private String hotelId;
    private String name;
    private String chainCode;
    private String iataCode;
    private String address;
    private String cityName;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    private Double rating;       // from ratings API if loaded
    private String ratingType;   // e.g. "review"

    public AmadeusHotelInfo() {}

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChainCode() { return chainCode; }
    public void setChainCode(String chainCode) { this.chainCode = chainCode; }

    public String getIataCode() { return iataCode; }
    public void setIataCode(String iataCode) { this.iataCode = iataCode; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getRatingType() { return ratingType; }
    public void setRatingType(String ratingType) { this.ratingType = ratingType; }
}

