package com.sparta.delivery.entity;

import java.math.BigDecimal;

public class Point {
    private final BigDecimal longitude;
    private final BigDecimal latitude;

    public Point(BigDecimal longitude, BigDecimal latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }
}