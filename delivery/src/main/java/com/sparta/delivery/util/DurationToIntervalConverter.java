package com.sparta.delivery.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter(autoApply = true)
public class DurationToIntervalConverter implements AttributeConverter<Duration, String> {

    @Override
    public String convertToDatabaseColumn(Duration attribute) {
        if (attribute == null) {
            return null;
        }
        long seconds = attribute.getSeconds();
        long absSeconds = Math.abs(seconds);
        long hours = absSeconds / 3600;
        long minutes = (absSeconds % 3600) / 60;
        long secs = absSeconds % 60;

        return String.format("%s%02d:%02d:%02d",
                seconds < 0 ? "-" : "",
                hours, minutes, secs);
    }

    @Override
    public Duration convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            String[] parts = dbData.split(":");
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            long seconds = Long.parseLong(parts[2]);
            return Duration.ofSeconds(hours * 3600 + minutes * 60 + seconds);
        } catch (Exception e) {
            throw new IllegalArgumentException("convertToEntityAttribute failed: " + dbData, e);
        }
    }
}