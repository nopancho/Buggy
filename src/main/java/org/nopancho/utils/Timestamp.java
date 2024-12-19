package org.nopancho.utils;

import ws.palladian.helper.date.DateHelper;

public class Timestamp {
    private Long time;

    public long setTime() {
        time = System.currentTimeMillis();
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Long getTime() {
        return time;
    }

    public String getFormatted() {
        String timeString = null;
        if (time != null) {
            timeString = DateHelper.getDatetime(time);
        }
        return timeString;
    }
}
