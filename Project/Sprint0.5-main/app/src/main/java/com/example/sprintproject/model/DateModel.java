package com.example.sprintproject.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;

public class DateModel {
    private DateModel() {
        
    }
    private static final MutableLiveData<Date> CURRENT_DATE = new MutableLiveData<>(new Date());
    public static LiveData<Date> getCurrentDate() {
        return CURRENT_DATE;
    }
    public static void setCurrentDate(Date date) {
        CURRENT_DATE.setValue(date);
    }
}

