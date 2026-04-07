package com.example.annoncebts.models;

import androidx.room.TypeConverter;
import java.util.Date;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromTypeNotification(TypeNotification type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static TypeNotification toTypeNotification(String value) {
        return value == null ? null : TypeNotification.valueOf(value);
    }

    @TypeConverter
    public static String fromRole(Role role) {
        return role == null ? null : role.name();
    }

    @TypeConverter
    public static Role toRole(String value) {
        return value == null ? null : Role.valueOf(value);
    }
}
