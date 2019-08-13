package com.example.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UptimeStats {

    @JsonIgnore
    private final RuntimeMXBean runtimeMXBean;

    @JsonProperty
    private long jvmUptime_Ms;

    @JsonProperty
    private String jvmStartTime;

    @JsonCreator
    public UptimeStats() {
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        jvmUptime_Ms = runtimeMXBean.getUptime();
        DateFormat df = new SimpleDateFormat("dd:MM:yy:HH:mm:ss");
        jvmStartTime = df.format(new Date(runtimeMXBean.getStartTime()));
    }
}
