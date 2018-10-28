package com.kaideveloper.weather.common;

public class WeatherTable {
    public static final String TABLE = "weather";

    public static class COLUMN {
        public static final String ID = "_id";
        public static final String VALUE = "value";
    }

    public static final String CREATE_SCRIPT =
            String.format("create table %s ("
                            + "%s integer primary key autoincrement,"
                            + "%s text" + ");",
                    TABLE, COLUMN.ID, COLUMN.VALUE);
}
