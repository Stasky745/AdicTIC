package com.adictic.common.util;

import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converters {

    @TypeConverter
    public static ArrayList<Integer> fromString(String value) {
        Type listType = new TypeToken<ArrayList<Integer>>(){}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromIntegerList(ArrayList<Integer> list) {
        return new Gson().toJson(list);
    }
}
