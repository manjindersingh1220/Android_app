package com.example.caipsgcms;

public class Timer {
    String time,load;

    public Timer() {
    }

    public Timer(String time, String load) {
        this.time = time;
        this.load = load;
    }

    public String getLoad() {
        return load;
    }

    public void setLoad(String load) {
        this.load = load;
    }

    public Timer(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
