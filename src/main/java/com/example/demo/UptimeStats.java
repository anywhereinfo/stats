package com.example.demo;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import com.example.demo.util.TimeUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UptimeStats {

    @JsonIgnore
    private final RuntimeMXBean runtimeMXBean;

    @JsonProperty
    private long jvmUptimeMs;

    @JsonProperty
    private String jvmStartTime;

    @JsonCreator
    public UptimeStats() {
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        jvmUptimeMs = runtimeMXBean.getUptime();      
        jvmStartTime = TimeUtils.fromTime(runtimeMXBean.getStartTime() );
    }
}
